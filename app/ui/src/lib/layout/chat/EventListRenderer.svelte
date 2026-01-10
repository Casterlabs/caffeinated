<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';
	import type { KoiEvent, MetaId } from '$lib/koi';
	import { fade } from 'svelte/transition';

	import EventRenderer, { SUPPORTED_EVENTS } from '$lib/layout/chat/EventRenderer.svelte';
	import { IconPause } from '@casterlabs/heroicons-svelte';
	import { DynamicList } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	const DYNAMIC_LIST_BLEED = 3;

	const HIGHLIGHT_FLASH_TIME = 750;
	const HIGHLIGHT_ANIMATE_TIME = 500;

	interface Props {
		uiEvents: EventHandler;
	}

	let { uiEvents }: Props = $props();

	let isAtBottom: boolean = $state(true);
	let dynamicList: DynamicList;
	// svelte-ignore non_reactive_update
	let scrollContainer: HTMLElement = {} as HTMLElement;

	// TODO some avatar bubble or something similar for LIKE events.

	onMount(() => {
		function handle(e: KoiEvent) {
			if (!e.event_type) return; // ?
			const canRenderEvent = SUPPORTED_EVENTS.includes(e.event_type.toUpperCase());
			const isUserClear = e.event_type == 'CLEARCHAT' && e.clear_type == 'USER';
			if (!canRenderEvent || isUserClear) {
				return;
			}

			dynamicList.addItem(e);
			recomputeEvens();
		}

		Koi.history().then((e) => e.forEach(handle));
		return Koi.on('*', (type: string, e: KoiEvent) => handle(e));
	});

	onMount(() => {
		// Bunch of spaghetti, because scrollIntoView() does not have a callback :(
		let scrollAnimationTarget: HTMLDivElement | null = null;
		let scrollAnimationTimeout: number | undefined;

		function animateHighlight() {
			if (!scrollAnimationTarget) return; // So typescript will shut up.

			// Clear the timeout just in case.
			clearTimeout(scrollAnimationTimeout);
			scrollAnimationTimeout = undefined;

			// Change the background color, letting CSS transition/animate it.
			scrollAnimationTarget.style.backgroundColor = 'var(--color-accent-5)';

			// Wait a bit, and then remove the background color, again letting CSS handle it.
			setTimeout(() => {
				if (!scrollAnimationTarget) return; // So typescript will shut up.
				scrollAnimationTarget.style.backgroundColor = '';
				scrollAnimationTarget = null;
			}, HIGHLIGHT_FLASH_TIME);
		}

		// When we trigger scrollIntoView(), we'll start receiving `scroll` events. So we set a timer for 100ms
		// If the DOM fires another `scroll` event, then we cancel said timer and restart it.
		// Effectively, this behaves like a callback for scrollIntoView().
		function scrollListener() {
			if (!scrollAnimationTarget) return; // So typescript will shut up.
			clearTimeout(scrollAnimationTimeout);
			scrollAnimationTimeout = setTimeout(animateHighlight, 100);
		}

		function startHighlight(e: CustomEvent) {
			if (scrollAnimationTarget) return;

			const id = e.detail as MetaId;
			const targetElement = scrollContainer.querySelector(`[data-clui-list-item-id="${id}"]`) as HTMLDivElement;
			if (!targetElement) return; // Element may have been removed from the DOM at this point.

			targetElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });

			scrollAnimationTarget = targetElement;
			scrollAnimationTimeout = setTimeout(animateHighlight, 100);
		}

		function jumpToBottom() {
			if (scrollAnimationTarget) return; // We do not want to allow the user to jump down if we're in the middle of a highlight.
			dynamicList.jumpToStart();
		}

		// @ts-ignore
		window.addEventListener('x-find-message', startHighlight);
		window.addEventListener('x-jump-bottom', jumpToBottom);
		scrollContainer.addEventListener('scroll', scrollListener);

		return () => {
			// @ts-ignore
			window.removeEventListener('x-find-message', startHighlight);
			window.removeEventListener('x-jump-bottom', jumpToBottom);
			scrollContainer.removeEventListener('scroll', scrollListener);
		};
	});

	export function recomputeEvens(invalidate = false) {
		const children = scrollContainer.children!;
		let endAtIdx = children.length;
		let evenOddIdx = 0;

		if (!invalidate) {
			// First, crawl the list in reverse order to figure out if the previously touched element was even or odd.
			// We bail out out once we find an element that we've already touched.
			// We do all of this malarky so that we aren't traversing (and potentially recomputing layout) for hundreds of elements.
			// NB: With InvertedScroller, the last element is at index 0.
			for (let idx = 0; idx < children.length; idx++) {
				const child = children.item(idx)!;

				if (child.getAttribute('data-touched') == 'true') {
					// We found it! Our evenOddIdx is now a sane value!
					endAtIdx = idx;

					if (!child.classList.contains('even-child')) {
						evenOddIdx++;
					}
					break;
				}
			}
		} // Otherwise, start at idx=0 and recompute the entire tree.

		for (let idx = 0; idx < endAtIdx; idx++) {
			const child = children.item(idx)!;
			const isVisible = getComputedStyle(child.querySelector('.event-renderer')!).display != 'none';
			if (!isVisible) continue;

			evenOddIdx++;

			const isEven = evenOddIdx % 2 == 0;
			child.setAttribute('data-touched', 'true');

			if (isEven && !child.classList.contains('even-child')) {
				child.classList.add('even-child');
			}
			if (!isEven && child.classList.contains('even-child')) {
				child.classList.remove('even-child');
			}
		}
	}
</script>

{#snippet itemRenderer(event: KoiEvent)}
	<EventRenderer {event} {uiEvents} />
{/snippet}

<div class="contents" style:--highlightanimatetime="{HIGHLIGHT_ANIMATE_TIME}ms">
	<DynamicList
		bind:this={dynamicList}
		bind:scrollContainer
		inverted
		bleed={DYNAMIC_LIST_BLEED}
		{itemRenderer}
		itemIdGenerator={(event) => {
			return (event as any).meta_id || event; // Either meta_id or identity.
		}}
	/>
</div>

{#if !isAtBottom}
	<button
		in:fade
		out:fade
		class="bg-gray-base border-gray-700 absolute inset-x-2 bottom-2 flex items-center justify-center rounded-md border p-1 opacity-90"
		onclick={() => dynamicList.jumpToStart()}
	>
		Scrolling paused
		<IconPause theme="mini" />
	</button>
{/if}

<style>
	:global(.inverted-scroller) > :global(li) {
		transition: background-color var(--highlightanimatetime);
	}

	/* ---- Preferences---- */

	:global(.USERNAME-THEME .username > .er-name) {
		color: var(--primary11);
	}

	:global(.USERNAME-USER .username > .er-name) {
		color: var(--contrast-color);
	}

	:global(.USERNAME-PLATFORM .username > .er-name) {
		color: var(--platform-color);
	}

	:global(.ZEBRA-STRIPES .even-child .event-renderer) {
		background-color: var(--base2);
	}

	:global(.HIDE-TIMESTAMP .er-timestamp) {
		display: none;
	}

	:global(.HIDE-PROFILEPICTURES .er-profile-picture) {
		display: none;
	}

	:global(.HIDE-BADGES .er-badges) {
		display: none;
	}

	:global(.HIDE-PLATFORM .er-platform) {
		display: none;
	}

	:global(.HIDE-PRONOUNS .er-pronouns) {
		display: none;
	}

	/* ---- Events ---- */

	:global(.HIDE-CHAT .er-chat-event) {
		display: none;
	}

	:global(.HIDE-ACTIVITIES .er-activity-event) {
		display: none;
	}

	:global(.HIDE-VIEWERS .er-viewer-event) {
		display: none;
	}
</style>

<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';
	import { hashCode } from '$lib/hash';
	import type { KoiEvent, MetaId } from '$lib/koi';
	import { fade } from 'svelte/transition';

	import EventRenderer, { SUPPORTED_EVENTS } from '$lib/layout/chat/EventRenderer.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconPause } from '@casterlabs/heroicons-svelte';
	import { DynamicList } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	const DYNAMIC_LIST_BLEED = 3;

	const HIGHLIGHT_FLASH_TIME = 750;
	const HIGHLIGHT_ANIMATE_TIME = 500;

	interface Props {
		uiEvents: EventHandler;
		useCatchups?: boolean;
	}

	let { uiEvents, useCatchups = false }: Props = $props();

	let isAtStart = $state(true);
	let dynamicList: DynamicList;
	// svelte-ignore non_reactive_update
	let scrollContainer: HTMLElement = {} as HTMLElement;

	// TODO some avatar bubble or something similar for LIKE events.

	onMount(() => {
		function handle(e: KoiEvent) {
			if (!e.event_type) {
				// console.warn('Event without event_type:', e);
				return;
			}

			// if (e.event_type == 'CATCHUP' && useCatchups) {
			// 	for (const catchupEvent of e.events) {
			// 		handle(catchupEvent);
			// 	}
			// }

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

			const backgroundColorTarget = scrollAnimationTarget.querySelector('.event-renderer') as HTMLDivElement;
			scrollAnimationTarget = null;

			// Change the background color, letting CSS transition/animate it.
			backgroundColorTarget.style.backgroundColor = 'var(--primary5) !important';

			// Wait a bit, and then remove the background color, again letting CSS handle it.
			setTimeout(() => {
				backgroundColorTarget.style.backgroundColor = '';
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

		function startHighlight(id: MetaId) {
			if (scrollAnimationTarget) return;

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
		const id1 = uiEvents.on('x-find-message', startHighlight);
		const id2 = uiEvents.on('x-jump-bottom', jumpToBottom);
		scrollContainer.addEventListener('scroll', scrollListener);

		return () => {
			id1();
			id2();
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
			const eventRenderer = child.querySelector('.event-renderer');
			if (!eventRenderer) continue;

			const isVisible = getComputedStyle(eventRenderer).display != 'none';
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

<div class="contents select-auto" style:--highlightanimatetime="{HIGHLIGHT_ANIMATE_TIME}ms">
	<DynamicList
		bind:this={dynamicList}
		bind:scrollContainer
		bind:isAtStart
		inverted
		bleed={DYNAMIC_LIST_BLEED}
		{itemRenderer}
		itemIdGenerator={(event) => {
			return (event as any).meta_id || /*(event as any).UEID ||*/ 'hc:' + hashCode(event);
		}}
	/>
</div>

{#if !isAtStart}
	<button
		in:fade
		out:fade
		class="bg-base-2 border-base-7 absolute inset-x-2 bottom-2 flex items-center justify-center rounded-md border p-1 opacity-90"
		onclick={() => dynamicList.jumpToStart()}
	>
		<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.scrolling_paused" />
		<IconPause theme="micro" />
	</button>
{/if}

<style>
	:global(.inverted-scroller) > :global(li .event-renderer) {
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

	:global(.HIDE-TIMESTAMP .er-timestamp) {
		display: none !important;
	}

	:global(.HIDE-PROFILEPICTURES .er-profile-picture) {
		display: none !important;
	}

	:global(.HIDE-BADGES .er-badges) {
		display: none !important;
	}

	:global(.HIDE-PLATFORM .er-platform) {
		display: none !important;
	}

	:global(.HIDE-PRONOUNS .er-pronouns) {
		display: none !important;
	}

	/* ---- Events ---- */

	:global(.HIDE-CHAT .er-chat-event) {
		display: none !important;
	}

	:global(.HIDE-ACTIVITIES .er-activity-event) {
		display: none !important;
	}

	:global(.HIDE-VIEWERS .er-viewer-event) {
		display: none !important;
	}
</style>

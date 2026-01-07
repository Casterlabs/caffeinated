<script lang="ts">
	import FocusListener from '$lib/interaction/FocusListener.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import type { IconProps } from '@casterlabs/heroicons-svelte';

	import { type Component, onMount } from 'svelte';

	const MIN_MARGIN = 8;

	interface ContextMenuItem {
		action: () => void;
		disabled?: boolean;
		label: string;
		icon?: Component<
			IconProps,
			{
				NAME: string;
			},
			''
		>;
	}

	interface Props {
		items: ContextMenuItem[];
		onclose?: () => void;
	}

	let { items, onclose }: Props = $props();

	let tiedToElement: HTMLElement | null = null;
	let position = $state([-1000, -1000]);
	let lastKnownScreenSize = $state([0, 0]);

	let container: HTMLElement;

	function updatePosition() {
		if (!tiedToElement) {
			return;
		}

		const x = tiedToElement.getBoundingClientRect().right;
		const y = tiedToElement.getBoundingClientRect().top;

		const newX = x + container.offsetWidth + MIN_MARGIN > window.innerWidth ? window.innerWidth - container.offsetWidth - MIN_MARGIN : x;
		const newY = y + container.offsetHeight + MIN_MARGIN > window.innerHeight ? window.innerHeight - container.offsetHeight - MIN_MARGIN : y;
		lastKnownScreenSize = [window.innerWidth, window.innerHeight];
		position = [newX, newY];
	}

	export function spawn(event: MouseEvent) {
		event.preventDefault();
		event.stopPropagation();

		container.classList.remove('hidden');
		tiedToElement = event.target as HTMLElement;
		updatePosition();

		container.querySelector('button')?.focus({
			preventScroll: true
		});
	}

	export function close() {
		onclose?.();

		// Move the context menu offscreen to avoid accidental clicks
		container.classList.add('hidden');
		tiedToElement = null;
		position = [-1000, -1000];
	}

	onMount(() => {
		let running = true;

		(function frame() {
			if (!running) return;

			const isOffScreen = position[0] == -1000 || position[1] == -1000;
			const hasScreenChanged = lastKnownScreenSize[0] !== window.innerWidth || lastKnownScreenSize[1] !== window.innerHeight;

			if (!isOffScreen && hasScreenChanged) {
				updatePosition();
			}

			requestAnimationFrame(frame);
		})();

		return () => (running = false);
	});
</script>

<FocusListener on:lostfocus={close}>
	<div
		bind:this={container}
		class="absolute text-base-12 z-50 bg-base-1 border border-base-6 rounded shadow-md w-max divide-y divide-base-6 hidden"
		style:left="{position[0]}px"
		style:top="{position[1]}px"
		role="menu"
	>
		{#each items as item}
			<button
				class="w-full text-left px-3 py-2 hover:bg-base-4 disabled:text-base-11 disabled:cursor-not-allowed flex items-center space-x-2"
				role="menuitem"
				onclick={() => {
					if (!item.disabled) {
						item.action();
						close();
					}
				}}
				disabled={item.disabled}
			>
				{#if item.icon}
					{@const Icon = item.icon}
					<span>
						<Icon theme="micro" />
					</span>
				{/if}

				<span>
					<LocalizedText key={item.label} />
				</span>
			</button>
		{/each}
	</div>
</FocusListener>

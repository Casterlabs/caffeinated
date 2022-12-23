<svelte:options accessors />

<script>
	import { onMount, createEventDispatcher, tick, SvelteComponent } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('CustomResizableGrid');

	const dispatch = createEventDispatcher();

	let /** @type {HTMLElement} */ container;

	/* -------------------- */
	/* Mounting & Creation  */
	/* -------------------- */

	let vlayout = [0.25, 0.33]; // last item is implied.
	let hlayout = [0.33, 0.45]; // last item is implied.
	$: width = vlayout.length + 1;
	$: height = hlayout.length + 1;

	/** @type {String | SvelteComponent} */
	export let contents = {}; // "x,y" = ...

	function onLayoutUpdated(remount = true) {
		// Notify of our updated layout.
		dispatch('update', { v: [...vlayout], h: [...hlayout] });

		if (remount) {
			// Try to remount the contents.
			tick();
		}
	}

	onMount(onLayoutUpdated);

	export function updateLayout(newLayout) {
		vlayout = newLayout.v;
		hlayout = newLayout.h;
		tick().then(onLayoutUpdated);
	}

	/* -------------------- */
	/* Dragging & Resizing  */
	/* -------------------- */

	const MINIMUM_DISTANCE = 12; /*px*/

	let isDraggingSizer = false;
	let isDraggingVerticalSizer = false;
	let draggingWhich = 0; // either the x or y border depending on the above variable.

	export let isLocked = true;

	function onMouseMove(e) {
		if (!isDraggingSizer) return;

		const {
			x: containerOffsetX,
			y: containerOffsetY,
			width: containerWidth,
			height: containerHeight
		} = container.getBoundingClientRect();
		const { clientX: mouseX, clientY: mouseY } = e;

		// We add 3.5 to X to keep the sizer bar centered on the mouse.
		const mousePosition = [
			(mouseX + 3.5 - containerOffsetX) / containerWidth,
			(mouseY - containerOffsetY) / containerHeight
		];

		const minDistance = MINIMUM_DISTANCE / containerWidth;
		const maxDistance = (containerWidth - MINIMUM_DISTANCE) / containerWidth;

		const relevantPosition = // Clamp
			Math.min(Math.max(mousePosition[isDraggingVerticalSizer ? 0 : 1], minDistance), maxDistance);

		const currentLayout = isDraggingVerticalSizer ? vlayout : hlayout;

		let subOther = 0;
		for (let i = 0; i < draggingWhich; i++) {
			subOther += currentLayout[i];
		}

		const lastPosition = currentLayout[draggingWhich];
		const currentPosition = relevantPosition - subOther;
		const positionDelta = lastPosition - currentPosition;

		if (positionDelta == 0) {
			return; // No change, do NOT rerender or do anything.
		}

		// Is it the last sizer bar? If so, don't apply any adjustments.
		if (draggingWhich != currentLayout.length - 1) {
			// Eat or Add some space to the next item.
			currentLayout[draggingWhich + 1] += positionDelta;
		}

		currentLayout[draggingWhich] = currentPosition;

		// Tell Svelte to rerender.
		vlayout = vlayout;
		hlayout = hlayout;
	}

	function onMouseUp() {
		isDraggingSizer = false;
		onLayoutUpdated(false);
	}

	function onMouseDown(e, isVertical, which) {
		if (isLocked) return;
		isDraggingSizer = true;
		isDraggingVerticalSizer = isVertical;
		draggingWhich = which;
		onMouseMove(e);
	}
</script>

<svelte:window on:mousemove={onMouseMove} on:mouseup={onMouseUp} />

<div class="hlayout" bind:this={container}>
	{#each Array(height) as _, hindex}
		{@const isHLast = hindex == height - 1}
		{@const hSize = isHLast ? 1 - hlayout.reduce((p, i) => p + i, 0) : hlayout[hindex]}

		<div class="hslot-area" style="--size: {hSize};">
			<div class="vlayout">
				{#each Array(width) as _, vindex}
					{@const isVLast = vindex == width - 1}
					{@const vSize = isVLast ? 1 - vlayout.reduce((p, i) => p + i, 0) : vlayout[vindex]}
					{@const location = `${vindex},${hindex}`}

					{@const content = contents[location] || ''}

					<div class="vslot-area" style="--size: {vSize};" data-position={location}>
						{#if typeof content == 'string'}
							{@html content}
						{:else}
							<svelte:component this={content} />
						{/if}
					</div>

					{#if !isVLast}
						<!-- Add the ver sizer everywhere except the last item. -->
						<div
							class="vsizer-bar oversize"
							class:cursor-col-resize={!isLocked}
							on:mousedown={(e) => onMouseDown(e, true, vindex)}
						>
							<div class="inner" />
						</div>
					{/if}
				{/each}
			</div>
		</div>

		{#if !isHLast}
			<!-- Add the hoz sizer everywhere except the last item. -->
			<div
				class="hsizer-bar"
				class:cursor-row-resize={!isLocked}
				on:mousedown={(e) => onMouseDown(e, false, hindex)}
			>
				<div class="inner" />
			</div>
		{/if}
	{/each}
</div>

<style>
	.hlayout {
		--sizer-bar-size: 7px;
		display: flex;
		flex-direction: column;
		width: 100%;
		height: 100%;
	}

	.hslot-area {
		width: 100%;
		height: calc(var(--size) * 100% - var(--sizer-bar-size) / 2);
	}

	.hsizer-bar {
		width: 100%;
		height: var(--sizer-bar-size);
	}

	.hsizer-bar .inner {
		width: 100%;
		margin-top: 3px;
		background-color: var(--base11);
		height: 1px;
	}

	.vlayout {
		display: flex;
		flex-direction: row;
		width: 100%;
		height: 100%;
	}

	.vslot-area {
		overflow: hidden;
		height: 100%;
		width: calc(var(--size) * 100% - var(--sizer-bar-size));
	}

	.vsizer-bar {
		height: 100%;
		width: var(--sizer-bar-size);
	}

	.vsizer-bar .inner {
		height: 100%;
		margin-left: 3px;
		background-color: var(--base11);
		width: 1px;
	}

	.vsizer-bar.oversize .inner {
		height: calc(100% + var(--sizer-bar-size));
		margin-left: 3px;
		background-color: var(--base11);
		width: 1px;
	}
</style>

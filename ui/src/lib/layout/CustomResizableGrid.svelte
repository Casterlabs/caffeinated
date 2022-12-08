<svelte:options accessors />

<script>
	import { onMount, createEventDispatcher, tick } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('CustomResizableGrid');

	const dispatch = createEventDispatcher();

	let vlayout = [0.25, 0.33]; // last item is implied.
	let hlayout = [0.33, 0.25]; // last item is implied.
	$: width = vlayout.length + 1;
	$: height = hlayout.length + 1;

	export let slotContents = {}; // <:element />
	let slotElements = {}; // <div />

	export function doMount() {
		// Loop bind the slotContents to slotElements.
		for (const [location, div] of Object.entries(slotElements)) {
			const contents = slotContents[location];

			if (!div || !contents) continue;

			console.debug('Mounting slot:', location, div, contents);
			div.appendChild(contents);
		}
	}

	function onLayoutUpdated() {
		// Notify of our updated layout.
		dispatch('update', { v: [...vlayout], h: [...hlayout] });

		// Automatically try to mount the contents.
		tick().then(doMount);
	}

	onMount(onLayoutUpdated);

	export function updateLayout(newLayout) {
		vlayout = newLayout.v;
		hlayout = newLayout.h;
		tick().then(onLayoutUpdated);
	}
</script>

<div class="hlayout">
	{#each Array(height) as _, hindex}
		{@const isHLast = hindex == height - 1}
		{@const hSize = isHLast ? 1 - hlayout.reduce((p, i) => p + i, 0) : hlayout[hindex]}

		<div class="hslot-area" style="--size: {hSize};">
			<div class="vlayout">
				{#each Array(width) as _, vindex}
					{@const isVLast = vindex == width - 1}
					{@const vSize = isVLast ? 1 - vlayout.reduce((p, i) => p + i, 0) : vlayout[vindex]}
					{@const location = `${vindex},${hindex}`}

					<div
						class="vslot-area"
						style="--size: {vSize};"
						bind:this={slotElements[location]}
						data-position={location}
					/>

					{#if !isVLast}
						<!-- Add the ver sizer everywhere except the last item. -->
						<div class="vsizer-bar oversize">
							<div class="inner" />
						</div>
					{/if}
				{/each}
			</div>
		</div>

		{#if !isHLast}
			<!-- Add the hoz sizer everywhere except the last item. -->
			<div class="hsizer-bar">
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
		cursor: row-resize;
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
		cursor: col-resize;
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

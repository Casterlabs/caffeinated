<script>
	import { onMount, createEventDispatcher, tick } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('CustomResizableGrid');

	const dispatch = createEventDispatcher();

	let vlayout = [0.25, 0.33]; // last item is implied.
	let hlayout = [0.33, 0.25]; // last item is implied.
	$: width = vlayout.length + 1;
	$: height = hlayout.length + 1;

	let slotContents = {}; // <slot />
	let slotElements = {}; // <div />

	function onLayoutUpdated() {
		// Loop bind the slotContents to slotElements.
		for (const [location, div] of Object.entries(slotElements)) {
			const contents = slotContents[location].firstElementChild;

			console.debug('Mounting slot:', location, div, contents);
			div.firstElementChild?.remove();
			div.appendChild(contents);
		}

		// Notify of our updated layout.
		dispatch('update', { v: [...vlayout], h: [...hlayout] });
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

<div style="display: none;" aria-hidden="true">
	<!-- We need 10 of these -->
	<span bind:this={slotContents['0,0']}><slot name="0,0" /></span>
	<span bind:this={slotContents['1,0']}><slot name="1,0" /></span>
	<span bind:this={slotContents['2,0']}><slot name="2,0" /></span>
	<span bind:this={slotContents['3,0']}><slot name="3,0" /></span>
	<span bind:this={slotContents['4,0']}><slot name="4,0" /></span>
	<span bind:this={slotContents['5,0']}><slot name="5,0" /></span>
	<span bind:this={slotContents['0,1']}><slot name="0,1" /></span>
	<span bind:this={slotContents['1,1']}><slot name="1,1" /></span>
	<span bind:this={slotContents['2,1']}><slot name="2,1" /></span>
	<span bind:this={slotContents['3,1']}><slot name="3,1" /></span>
	<span bind:this={slotContents['4,1']}><slot name="4,1" /></span>
	<span bind:this={slotContents['5,1']}><slot name="5,1" /></span>
	<span bind:this={slotContents['0,2']}><slot name="0,2" /></span>
	<span bind:this={slotContents['1,2']}><slot name="1,2" /></span>
	<span bind:this={slotContents['2,2']}><slot name="2,2" /></span>
	<span bind:this={slotContents['3,2']}><slot name="3,2" /></span>
	<span bind:this={slotContents['4,2']}><slot name="4,2" /></span>
	<span bind:this={slotContents['5,2']}><slot name="5,2" /></span>
	<span bind:this={slotContents['0,3']}><slot name="0,3" /></span>
	<span bind:this={slotContents['1,3']}><slot name="1,3" /></span>
	<span bind:this={slotContents['2,3']}><slot name="2,3" /></span>
	<span bind:this={slotContents['3,3']}><slot name="3,3" /></span>
	<span bind:this={slotContents['4,3']}><slot name="4,3" /></span>
	<span bind:this={slotContents['5,3']}><slot name="5,3" /></span>
	<span bind:this={slotContents['0,4']}><slot name="0,4" /></span>
	<span bind:this={slotContents['1,4']}><slot name="1,4" /></span>
	<span bind:this={slotContents['2,4']}><slot name="2,4" /></span>
	<span bind:this={slotContents['3,4']}><slot name="3,4" /></span>
	<span bind:this={slotContents['4,4']}><slot name="4,4" /></span>
	<span bind:this={slotContents['5,4']}><slot name="5,4" /></span>
	<span bind:this={slotContents['0,5']}><slot name="0,5" /></span>
	<span bind:this={slotContents['1,5']}><slot name="1,5" /></span>
	<span bind:this={slotContents['2,5']}><slot name="2,5" /></span>
	<span bind:this={slotContents['3,5']}><slot name="3,5" /></span>
	<span bind:this={slotContents['4,5']}><slot name="4,5" /></span>
	<span bind:this={slotContents['5,5']}><slot name="5,5" /></span>
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

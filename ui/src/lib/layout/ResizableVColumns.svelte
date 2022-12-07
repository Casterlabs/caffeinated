<script>
	import { onMount } from 'svelte';

	let layout = [0.33, 0.33]; // third item has implied size.

	export let onLayoutMount;
	onMount(() => onLayoutMount(updateLayout));

	export function updateLayout(newLayout) {
		console.debug(newLayout);
	}
</script>

<div class="vlayout">
	<div class="vslot-area" style="--size: {layout[0]};"><slot name="0" /></div>

	<div class="vsizer-bar">
		<div class="inner" />
	</div>

	<div class="vslot-area" style="--size: {layout[1]};"><slot name="1" /></div>

	<div class="vsizer-bar">
		<div class="inner" />
	</div>

	<div class="vslot-area" style="--size: {1 - layout[0] - layout[1]};"><slot name="2" /></div>
</div>

<style>
	.vlayout {
		--sizer-bar-size: 7px;
		display: flex;
		flex-direction: row;
		width: 100%;
		height: 100%;
		overflow: hidden;
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
</style>

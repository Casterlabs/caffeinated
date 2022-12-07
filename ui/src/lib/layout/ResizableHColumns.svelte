<script>
	import { onMount } from 'svelte';

	let layout = [0.33, 0.33]; // third item has implied size.

	export let onLayoutMount;
	onMount(() => onLayoutMount(updateLayout));

	export function updateLayout(newLayout) {
		console.debug(newLayout);
	}
</script>

<div class="hlayout">
	<div class="hslot-area" style="--size: {layout[0]};"><slot name="0" /></div>

	<div class="hsizer-bar">
		<div class="inner" />
	</div>

	<div class="hslot-area" style="--size: {layout[1]};"><slot name="1" /></div>

	<div class="hsizer-bar">
		<div class="inner" />
	</div>

	<div class="hslot-area" style="--size: {1 - layout[0] - layout[1]};"><slot name="2" /></div>
</div>

<style>
	.hlayout {
		--sizer-bar-size: 7px;
		display: flex;
		flex-direction: column;
		width: 100%;
		height: 100%;
		overflow: hidden;
	}

	.hslot-area {
		overflow: hidden;
		width: 100%;
		height: calc(var(--size) * 100% - var(--sizer-bar-size));
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
</style>

<script>
	import { onMount } from 'svelte';

	let layout = [0.5, 0.5]; // w, h

	export let onLayoutMount;
	onMount(() => onLayoutMount(updateLayout));

	export function updateLayout(newLayout) {
		console.debug(newLayout);
	}
</script>

<div class="hlayout">
	<div class="hslot-area" style="--size: {layout[1]};">
		<div class="vlayout">
			<div class="vslot-area" style="--size: {layout[0]};"><slot name="0" /></div>

			<div class="vsizer-bar oversize">
				<div class="inner" />
			</div>

			<div class="vslot-area" style="--size: {1 - layout[0]};"><slot name="1" /></div>
		</div>
	</div>

	<div class="hsizer-bar">
		<div class="inner" />
	</div>

	<div class="hslot-area" style="--size: {1 - layout[1]};">
		<div class="vlayout">
			<div class="vslot-area" style="--size: {layout[0]};"><slot name="2" /></div>

			<div class="vsizer-bar">
				<div class="inner" />
			</div>

			<div class="vslot-area" style="--size: {1 - layout[0]};"><slot name="3" /></div>
		</div>
	</div>
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

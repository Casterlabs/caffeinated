<script>
	import PageTitle from '../components/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/CustomResizableGrid.svelte';
	import NumberInput from '$lib/ui/NumberInput.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Dashboard');

	let layoutElement;
	let layout; // TODO load and save this to caffeinated.

	$: width = layout?.v?.length + 1 || 0;
	$: height = layout?.h?.length + 1 || 0;

	function onLayoutUpdate({ detail: _layout }) {
		layout = _layout;
		console.debug('Layout update:', layout);
		// TODO save
	}

	onMount(() => {
		// TODO load
	});
</script>

<PageTitle title="page.dashboard" />

<div class="fixed inset-0 left-48">
	<CustomResizableGrid bind:this={layoutElement} on:update={onLayoutUpdate}>
		<div slot="0,0">0,0</div>
		<div slot="1,0">1,0</div>
		<div slot="2,0">2,0</div>
		<div slot="3,0">3,0</div>
		<div slot="4,0">4,0</div>
		<div slot="5,0">5,0</div>
		<div slot="0,1">0,1</div>
		<div slot="1,1">1,1</div>
		<div slot="2,1">2,1</div>
		<div slot="3,1">3,1</div>
		<div slot="4,1">4,1</div>
		<div slot="5,1">5,1</div>
		<div slot="0,2">0,2</div>
		<div slot="1,2">1,2</div>
		<div slot="2,2">2,2</div>
		<div slot="3,2">3,2</div>
		<div slot="4,2">4,2</div>
		<div slot="5,2">5,2</div>
		<div slot="0,3">0,3</div>
		<div slot="1,3">1,3</div>
		<div slot="2,3">2,3</div>
		<div slot="3,3">3,3</div>
		<div slot="4,3">4,3</div>
		<div slot="5,3">5,3</div>
		<div slot="0,4">0,4</div>
		<div slot="1,4">1,4</div>
		<div slot="2,4">2,4</div>
		<div slot="3,4">3,4</div>
		<div slot="4,4">4,4</div>
		<div slot="5,4">5,4</div>
		<div slot="0,5">0,5</div>
		<div slot="1,5">1,5</div>
		<div slot="2,5">2,5</div>
		<div slot="3,5">3,5</div>
		<div slot="4,5">4,5</div>
		<div slot="5,5">5,5</div>
	</CustomResizableGrid>
</div>

<div
	class="fixed top-0 right-4 h-9 -translate-y-8 hover:translate-y-0 px-2 py-0.5 transition-all rounded-b-md ring-1 ring-base-7 bg-base-5 text-base-12 opacity-90"
>
	<div class="inline-block w-12">
		<NumberInput
			value={width}
			min="1"
			max="6"
			on:value={({ detail: newWidth }) => {
				if (newWidth < width) {
					layout.h.pop();
					layoutElement.updateLayout(layout);
				} else {
					// TODO
				}
				console.debug('New width:', newWidth);
			}}
		/>
	</div>
	x
	<div class="inline-block w-12">
		<NumberInput
			value={height}
			min="1"
			max="6"
			on:value={({ detail: newHeight }) => {
				if (newHeight < height) {
					layout.v.pop();
					layoutElement.updateLayout(layout);
				} else {
					// TODO
				}
				console.debug('New height:', newHeight);
			}}
		/>
	</div>
</div>

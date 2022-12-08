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

<div class="hidden">
	{#each Array(width) as _, hindex}
		{#each Array(height) as _, vindex}
			{@const location = `${vindex},${hindex}`}
			<div bind:this={layoutElement.slotContents[location]}>
				{location}
			</div>
		{/each}
	{/each}
</div>

<div class="fixed inset-0 left-48">
	<CustomResizableGrid bind:this={layoutElement} on:update={onLayoutUpdate} />
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

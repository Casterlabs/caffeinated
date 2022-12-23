<script>
	import PageTitle from '../components/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/CustomResizableGrid.svelte';
	import NumberInput from '$lib/ui/NumberInput.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const MAX = 6;

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
	<!-- This is yucky looking, ik. -->
	<div class="inline-block w-12">
		<NumberInput
			value={width}
			min={1}
			max={MAX}
			on:value={({ detail: newWidth }) => {
				const delta = newWidth - width;
				const isAdd = delta > 0;

				const newItemSize = 1 / newWidth;
				const oldItemSize = 1 / width;
				const scale = newItemSize / oldItemSize;
				console.log(scale);

				layout.v.forEach((value, idx) => {
					layout.v[idx] = value * scale;
				});

				if (isAdd) {
					for (let idx = 0; idx < Math.abs(delta); idx++) {
						layout.v.push(newItemSize);
					}
				} else {
					for (let idx = 0; idx < Math.abs(delta); idx++) {
						layout.v.pop();
					}
				}

				console.debug('New width:', newWidth);
				layoutElement.updateLayout(layout);
			}}
		/>
	</div>
	x
	<div class="inline-block w-12">
		<NumberInput
			value={height}
			min={1}
			max={MAX}
			on:value={({ detail: newHeight }) => {
				const delta = newHeight - height;
				const isAdd = delta > 0;

				const newItemSize = 1 / newHeight;
				const oldItemSize = 1 / height;
				const scale = newItemSize / oldItemSize;
				console.log(scale);

				layout.h.forEach((value, idx) => {
					layout.h[idx] = value * scale;
				});

				if (isAdd) {
					for (let idx = 0; idx < Math.abs(delta); idx++) {
						layout.h.push(newItemSize);
					}
				} else {
					for (let idx = 0; idx < Math.abs(delta); idx++) {
						layout.h.pop();
					}
				}

				console.debug('New height:', newHeight);
				layoutElement.updateLayout(layout);
			}}
		/>
	</div>
</div>

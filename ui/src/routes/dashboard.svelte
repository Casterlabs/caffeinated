<script>
	import PageTitle from '../components/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/ResizableGrid.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const MAX = 6;

	const console = createConsole('Dashboard');

	let layoutElement;
	let layout = {
		h: [0.25, 0.33],
		v: [0.33, 0.45]
	}; // TODO load and save this to caffeinated.

	let contents = {};
	for (let x = 0; x < MAX; x++) {
		for (let y = 0; y < MAX; y++) {
			contents[`${x},${y}`] = `${x},${y}`;
		}
	}

	function onLayoutUpdate({ detail: _layout }) {
		layout = _layout;
		console.debug('Layout update:', layout);
		// TODO save
	}

	onMount(() => {
		layoutElement.updateLayout(layout);
		// TODO load
	});
</script>

<PageTitle title="page.dashboard" />

<div class="fixed inset-0 left-48">
	<CustomResizableGrid
		bind:this={layoutElement}
		maxSize={MAX}
		{contents}
		on:update={onLayoutUpdate}
	/>
</div>

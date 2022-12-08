<script>
	import PageTitle from '../components/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/CustomResizableGrid.svelte';

	let layout;

	function onLayoutMount() {
		console.debug('Layout mount');
	}
</script>

<PageTitle title="page.dashboard" />

<div class="fixed inset-0 left-48">
	<CustomResizableGrid bind:this={layout} on:mount={onLayoutMount}>
		<div slot="0,0">0,0</div>
		<div slot="0,1">0,1</div>
		<div slot="0,2">0,2</div>
		<div slot="0,3">0,3</div>
		<div slot="0,4">0,4</div>
		<div slot="0,5">0,5</div>
		<div slot="1,0">1,0</div>
		<div slot="1,1">1,1</div>
		<div slot="1,2">1,2</div>
		<div slot="1,3">1,3</div>
		<div slot="1,4">1,4</div>
		<div slot="1,5">1,5</div>
		<div slot="2,0">2,0</div>
		<div slot="2,1">2,1</div>
		<div slot="2,2">2,2</div>
		<div slot="2,3">2,3</div>
		<div slot="2,4">2,4</div>
		<div slot="2,5">2,5</div>
		<div slot="3,0">3,0</div>
		<div slot="3,1">3,1</div>
		<div slot="3,2">3,2</div>
		<div slot="3,3">3,3</div>
		<div slot="3,4">3,4</div>
		<div slot="3,5">3,5</div>
		<div slot="4,0">4,0</div>
		<div slot="4,1">4,1</div>
		<div slot="4,2">4,2</div>
		<div slot="4,3">4,3</div>
		<div slot="4,4">4,4</div>
		<div slot="4,5">4,5</div>
		<div slot="5,0">5,0</div>
		<div slot="5,1">5,1</div>
		<div slot="5,2">5,2</div>
		<div slot="5,3">5,3</div>
		<div slot="5,4">5,4</div>
		<div slot="5,5">5,5</div>
	</CustomResizableGrid>
</div>

<div
	class="fixed top-0 right-4 h-7 -translate-y-6 hover:translate-y-0 px-2 py-0.5 transition-all rounded-b-md ring-1 ring-base-7 bg-base-5 text-base-12 opacity-90"
>
	TODO
</div>

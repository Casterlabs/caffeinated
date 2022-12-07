<script>
	import PageTitle from '../components/PageTitle.svelte';
	import ResizableGrid from '$lib/layout/ResizableGrid.svelte';
	import ResizableHColumns from '$lib/layout/ResizableHColumns.svelte';
	import ResizableVColumns from '$lib/layout/ResizableVColumns.svelte';

	let layout = 'GRID'; // GRID, HCOLUMNS, VCOLUMNS
	$: layoutClass = {
		GRID: ResizableGrid,
		HCOLUMNS: ResizableHColumns,
		VCOLUMNS: ResizableVColumns
	}[layout];

	let updateLayout = () => {};
	function onLayoutMount(_updateLayout) {
		updateLayout = _updateLayout;
		console.debug('Layout mount');
	}
</script>

<PageTitle title="page.dashboard" />

<div class="fixed inset-0 left-48">
	<svelte:component this={layoutClass} {onLayoutMount}>
		<div slot="0">0</div>
		<div slot="1">1</div>
		<div slot="2">2</div>
		<div slot="3">3</div>
	</svelte:component>
</div>

<div
	class="fixed top-0 right-4 h-7 -translate-y-6 hover:translate-y-0 px-2 py-0.5 transition-all rounded-b-md ring-1 ring-base-7 bg-base-5 text-base-12 opacity-90"
>
	<button class="scale-[1.1]" on:click={() => (layout = 'HCOLUMNS')}>
		<icon data-icon="icon/view-columns" />
	</button>
	<button
		class="rotate-90 scale-[.90] -translate-x-[0.03rem]"
		on:click={() => (layout = 'VCOLUMNS')}
	>
		<icon data-icon="icon/view-columns" />
	</button>
	<button on:click={() => (layout = 'GRID')}>
		<icon data-icon="icon/squares-2x2" />
	</button>
</div>

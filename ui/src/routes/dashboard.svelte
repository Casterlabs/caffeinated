<script>
	import PageTitle from '../components/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/ResizableGrid.svelte';
	import DashboardPiece from '$lib/layout/dashboard/DashboardPiece.svelte';
	import WelcomeWagon from '$lib/layout/dashboard/WelcomeWagon.svelte';
	import Chat from '../components/dashboard/Chat.svelte';
	import Viewers from '../components/dashboard/Viewers.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const MAX = 6;
	const console = createConsole('Dashboard');

	const components = {
		[null]: '',
		welcomewagon: WelcomeWagon,
		'co.casterlabs.dock.stream_chat.dock': Chat,
		'co.casterlabs.dock.viewers.dock': Viewers
	};

	const componentChoices = {
		[null]: 'dashboard.customize.options.none',
		// welcomewagon: 'WelcomeWagon',
		'co.casterlabs.dock.viewers.dock': 'Viewers' // Temporary.
	};

	let layoutElement;
	let currentLayout;

	/** @type {Map<string, [String | SvelteComponent, object?]>} */
	let contents = {};

	function save() {
		console.debug('Saving layout:', currentLayout);
		Caffeinated.UI.updateDashboard(currentLayout, true /*isMain*/);
	}

	function onLayoutUpdate({ detail: newLayout }) {
		if (!currentLayout) return;
		console.debug('Layout update:', newLayout);
		currentLayout.h = newLayout.h;
		currentLayout.v = newLayout.v;
		save();
	}

	function onPieceUpdate(location, value) {
		if (!currentLayout) return;
		console.debug('Piece update:', location, value);
		currentLayout.contents[location] = value;
		save();
	}

	onMount(async () => {
		// Load async.
		Caffeinated.plugins.widgets.then((widgets) => {
			for (const widget of widgets) {
				if (widget.details.type == 'DOCK') {
					componentChoices[widget.id] = widget.details.friendlyName;
					// We can get away with updating this because componentChoices is a pointer ;)
				}
			}
		});

		currentLayout = (await Caffeinated.UI.preferences).mainDashboard;
		console.log('Loaded layout:', currentLayout);

		// Fill all slots with DashboardPiece.
		for (let x = 0; x < MAX; x++) {
			for (let y = 0; y < MAX; y++) {
				const location = `${x},${y}`;
				const currentValue = currentLayout.contents[location];

				contents[location] = [
					DashboardPiece,
					{
						location,
						onPieceUpdate,
						grid: layoutElement,
						current: currentValue,
						components, // These are pointers ;)
						componentChoices // ^
					}
				];
			}
		}

		layoutElement.updateLayout(currentLayout);
	});
</script>

<PageTitle title="page.dashboard" />

<div class="fixed inset-0 left-[var(--sidebar-width)]">
	<CustomResizableGrid
		bind:this={layoutElement}
		maxSize={MAX}
		{contents}
		on:update={onLayoutUpdate}
	/>
</div>

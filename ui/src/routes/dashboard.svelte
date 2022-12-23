<script>
	import PageTitle from '../components/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/ResizableGrid.svelte';
	import DashboardPiece from '$lib/layout/dashboard/DashboardPiece.svelte';
	import WelcomeWagon from '$lib/layout/dashboard/WelcomeWagon.svelte';
	import Chat from '../components/Chat.svelte';
	import Viewers from '../components/Viewers.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const MAX = 6;
	const console = createConsole('Dashboard');

	const components = {
		[null]: '',
		welcomewagon: WelcomeWagon,
		'co.casterlabs.dock.stream_chat.dock': Chat,
	};

	const componentChoices = {
		[null]: 'dashboard.customize.options.none',
	};

	let layoutElement;

	/** @type {Map<string, [String | SvelteComponent, object?]>} */
	let contents = {};

	function onLayoutUpdate({ detail: newLayout }) {
		console.debug('Layout update:', newLayout);
		// TODO save
	}

	function onPieceUpdate(location, value) {
		// TODO save
		console.debug('Piece update:', location, value);
	}

	// const preferences = st || Caffeinated.UI.svelte('preferences');
	// $: preferences, $preferences && console.debug('UI Preferences:', $preferences);

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

		const { mainDashboard: layout } = await Caffeinated.UI.preferences;

		// Fill all slots with DashboardPiece.
		for (let x = 0; x < MAX; x++) {
			for (let y = 0; y < MAX; y++) {
				const location = `${x},${y}`;
				const currentValue = layout.contents[location];

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

		layoutElement.updateLayout(layout);

		// TODO parse the slot contents of the layout.

		console.log('Loaded layout:', layout);
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

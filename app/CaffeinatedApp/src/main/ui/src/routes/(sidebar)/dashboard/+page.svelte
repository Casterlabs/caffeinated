<script>
	import PageTitle from '$lib/layout/PageTitle.svelte';
	import CustomResizableGrid from '$lib/layout/ResizableGrid.svelte';
	import DashboardPiece from '$lib/layout/dashboard/DashboardPiece.svelte';

	import component_WelcomeWagon from '$lib/layout/dashboard/WelcomeWagon.svelte';
	import component_Chat from '$lib/dashboard/Chat.svelte';
	import component_Viewers from '$lib/dashboard/Viewers.svelte';
	import component_ChannelInfo from '$lib/dashboard/ChannelInfo/index.svelte';
	import component_ActivityFeed from '$lib/dashboard/ActivityFeed.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const MAX = 6;
	const console = createConsole('Dashboard');

	const components = {
		[null]: '',
		welcomewagon: component_WelcomeWagon,
		'co.casterlabs.dock.stream_chat.dock': component_Chat,
		'co.casterlabs.dock.viewers.dock': component_Viewers,
		// 'co.casterlabs.dock.channel_info.dock': component_ChannelInfo,
		'co.casterlabs.dock.activity_feed.dock': component_ActivityFeed
	};

	const componentChoices = {
		[null]: 'co.casterlabs.caffeinated.app.page.dashboard.customize.options.none'
		// welcomewagon: 'WelcomeWagon',
		// 'co.casterlabs.dock.channel_info.dock': 'Channel Info' // Temporary.
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
		Caffeinated.pluginIntegration.widgets.then((widgets) => {
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

<PageTitle title="co.casterlabs.caffeinated.app.page.dashboard" />

<div class="fixed inset-0 left-[var(--actual-sidebar-width)]">
	<CustomResizableGrid
		bind:this={layoutElement}
		maxSize={MAX}
		{contents}
		on:update={onLayoutUpdate}
	/>
</div>

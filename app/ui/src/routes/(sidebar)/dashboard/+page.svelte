<script lang="ts">
	import DashboardPiece from '$lib/layout/dashboard/DashboardPiece.svelte';
	import ResizableGrid from '$lib/layout/dashboard/ResizableGrid.svelte';
	import component_WelcomeWagon from '$lib/layout/dashboard/WelcomeWagon.svelte';
	import component_ActivityFeed from '../../(no-sidebar)/docks/activity-feed/+page.svelte';
	import component_ChannelInfo from '../../(no-sidebar)/docks/channel-info/+page.svelte';
	import component_Chat from '../../(no-sidebar)/docks/chat/+page.svelte';
	import component_Viewers from '../../(no-sidebar)/docks/viewers/+page.svelte';

	import { type Component, onMount } from 'svelte';

	const MAX = 6;

	interface AppLayout {
		h: number[];
		v: number[];
		contents: Record<string, string>;
	}

	const components: Record<string, Component<any, any, any>> = {
		// @ts-ignore
		[null]: '', // Ensure null is always present.

		welcomewagon: component_WelcomeWagon,
		'co.casterlabs.dock.stream_chat.dock': component_Chat,
		'co.casterlabs.dock.viewers.dock': component_Viewers,
		// 'co.casterlabs.dock.channel_info.dock': component_ChannelInfo,
		'co.casterlabs.dock.activity_feed.dock': component_ActivityFeed
	};

	const componentChoices: Record<string, string> = {
		// @ts-ignore
		[null]: 'co.casterlabs.caffeinated.app.page.dashboard.customize.options.none'

		// welcomewagon: 'WelcomeWagon',
		// 'co.casterlabs.dock.channel_info.dock': 'Channel Info' // Temporary.
	};

	let layoutElement: ResizableGrid;
	let currentLayout: AppLayout;

	let contents: Record<string, [String | Component<any, any, any>, object?]> = {};

	function save() {
		console.debug('Saving layout:', currentLayout);
		AppUI.updateDashboard(currentLayout, true /*isMain*/);
	}

	function onLayoutUpdate(newLayout: { h: number[]; v: number[] }) {
		if (!currentLayout) return;
		console.debug('Layout update:', newLayout);
		currentLayout.h = newLayout.h;
		currentLayout.v = newLayout.v;
		save();
	}

	function onPieceUpdate(location: string, value: string | null) {
		if (!currentLayout) return;
		console.debug('Piece update:', location, value);
		// @ts-ignore
		currentLayout.contents[location] = value;
		save();
	}

	onMount(async () => {
		// Load async.
		AppPlugins.widgets.then((widgets) => {
			for (const widget of widgets) {
				if (widget.details.type == 'DOCK') {
					componentChoices[widget.id] = widget.details.friendlyName;
					// We can get away with updating this because componentChoices is a pointer ;)
				}
			}
		});

		currentLayout = (await AppConfig.uiPreferences).mainDashboard;
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

		console.log(contents);

		layoutElement.updateLayout(currentLayout);
	});
</script>

<div class="fixed inset-0 left-[var(--actual-sidebar-width)]">
	<ResizableGrid bind:this={layoutElement} maxSize={MAX} {contents} onupdate={onLayoutUpdate} />
</div>

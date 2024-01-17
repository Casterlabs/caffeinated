<script>
	import ChatViewer from './Chat.svelte';
	import ViewersList from './ViewersList.svelte';

	export let chatViewer;
	export let viewersList;

	let showViewersList;

	export let doAction; // fn

	export function processEvent(event) {
		if (event.event_type == 'VIEWER_LIST') {
			viewersList.onViewersList(event);
		}

		chatViewer.processEvent(event);
	}

	export function loadConfig(config) {
		const { viewersX, viewersY, viewersWidth, viewersHeight } = config;
		viewersList.setPositionData(viewersX, viewersY, viewersWidth, viewersHeight);
		chatViewer.loadConfig(config);
		showViewersList = config.showViewersList;
	}
</script>

<ViewersList
	bind:this={viewersList}
	visible={showViewersList}
	on:copy={({ detail: data }) => doAction('copy-viewers', data)}
	on:update={chatViewer?.savePreferences}
/>

<ChatViewer bind:this={chatViewer} {doAction} />

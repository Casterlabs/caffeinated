<script>
	import { onMount } from 'svelte';
	import ViewersList from '$lib/chat/ViewersList.svelte';

	let listElement;

	onMount(() => {
		const eventListener = Bridge.on('koi:event', (event) => {
			if (event.event_type == 'VIEWER_LIST') {
				listElement.onViewersList(event);
			} else if (event.event_type == 'VIEWER_COUNT') {
				listElement.onViewersCount(event);
			}
		});

		Caffeinated.koi.viewers.then((all) => {
			for (const [platform, viewers] of Object.entries(all)) {
				listElement.onViewersList({
					streamer: {
						platform
					},
					viewers
				});
			}
		});

		Caffeinated.koi.viewerCounts.then((all) => {
			for (const [platform, count] of Object.entries(all)) {
				listElement.onViewersCount({
					streamer: {
						platform
					},
					count
				});
			}
		});

		return () => {
			Bridge.off('koi:event', eventListener);
		};
	});
</script>

<ViewersList
	bind:this={listElement}
	on:copy={({ detail: text }) => {
		window.Caffeinated.copyText(text, 'Copied the viewer list to your clipboard');
	}}
/>

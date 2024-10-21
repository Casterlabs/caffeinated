<script>
	import { onMount } from 'svelte';
	import ViewersList from '$lib/chat/ViewersList.svelte';

	let listElement;

	onMount(() => {
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

		const eventListener = window.saucer.messages.onMessage(([type, data]) => {
			if (type == 'koi:event') {
				if (data.event_type == 'VIEWER_LIST') {
					listElement.onViewersList(event);
				} else if (data.event_type == 'VIEWER_COUNT') {
					listElement.onViewersCount(event);
				}
			}
		});
		return () => {
			window.saucer.messages.off(eventListener);
		};
	});
</script>

<ViewersList
	bind:this={listElement}
	on:copy={({ detail: text }) => {
		window.Caffeinated.copyText(text, 'Copied the viewer list to your clipboard');
	}}
/>

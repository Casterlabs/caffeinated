<script>
	import { onMount } from 'svelte';
	import ViewersList from '$lib/chat/ViewersList.svelte';

	let listElement;

	onMount(() => {
		Koi.on('VIEWER_LIST', listElement.onViewersList);
		Koi.on('VIEWER_COUNT', listElement.onViewerCount);

		for (const [platform, viewers] of Object.entries(Koi.viewers)) {
			listElement.onViewersList({
				streamer: {
					platform
				},
				viewers
			});
		}

		for (const [platform, count] of Object.entries(Koi.viewerCounts)) {
			listElement.onViewersCount({
				streamer: {
					platform
				},
				count
			});
		}
	});
</script>

<ViewersList
	bind:this={listElement}
	on:copy={({ detail: text }) => {
		window.Widget.emit('copyText', text);
		alert('Copied the viewer list to your clipboard');
	}}
/>

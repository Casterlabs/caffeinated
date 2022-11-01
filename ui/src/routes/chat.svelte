<script>
	import PageTitle from '../components/PageTitle.svelte';
	import ChatViewer from '../components/chat/ChatViewer.svelte';

	import { onMount } from 'svelte';

	let chatViewer;

	onMount(() => {
		const eventListener = Bridge.on('koi:event', chatViewer.processEvent);
		Caffeinated.koi.eventHistory.then((h) => h.forEach(chatViewer.processEvent));

		return () => {
			Bridge.off('koi:event', eventListener);
		};
	});
</script>

<PageTitle title="page.chat" />

<ChatViewer bind:this={chatViewer} />

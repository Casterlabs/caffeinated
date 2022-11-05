<script>
	import PageTitle from '../components/PageTitle.svelte';
	import ChatViewer from '$lib/chat/ChatViewer.svelte';

	import { onMount } from 'svelte';

	let chatViewer;

	function doAction(action, data) {
		switch (action) {
			case 'chat': {
				const { platform, message, replyTarget } = data;
				Caffeinated.koi.sendChat(platform, message, 'CLIENT', replyTarget, true);
				return;
			}
			case 'delete': {
				const { platform, eventId } = data;
				Caffeinated.koi.deleteChat(platform, eventId, true);
				return;
			}
			case 'upvote': {
				const { platform, eventId } = data;
				Caffeinated.koi.upvoteChat(platform, eventId);
				return;
			}
		}
	}

	onMount(() => {
		const eventListener = Bridge.on('koi:event', chatViewer.processEvent);
		Caffeinated.koi.eventHistory.then((h) => h.forEach(chatViewer.processEvent));

		return () => {
			Bridge.off('koi:event', eventListener);
		};
	});
</script>

<PageTitle title="page.chat" />

<ChatViewer bind:this={chatViewer} {doAction} />

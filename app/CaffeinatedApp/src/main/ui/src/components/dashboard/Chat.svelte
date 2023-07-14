<script>
	import ChatViewer from '$lib/chat/Chat.svelte';

	import { onMount } from 'svelte';

	let chatViewer;

	const userStates = st || Caffeinated.koi.svelte('userStates');

	function doAction(action, data) {
		switch (action) {
			case 'save-preferences': {
				Caffeinated.UI.chatPreferences = data;
				return;
			}
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
		Caffeinated.koi.eventHistory.then((history) => {
			for (const event of history) {
				event.is_historical = true;
				chatViewer.processEvent(event);
			}
		});

		Caffeinated.koi.features.then((featuresByPlatform) => {
			console.debug('Supported features:', featuresByPlatform);
			chatViewer.supportedFeatures = featuresByPlatform;
		});

		// Kickstart.
		Caffeinated.UI.chatPreferences.then(chatViewer.loadConfig);

		// Unsubscribe when this page exits.
		const eventListener = Bridge.on('koi:event', chatViewer.processEvent);
		return () => {
			Bridge.off('koi:event', eventListener);
		};
	});
</script>

<ChatViewer bind:this={chatViewer} {doAction} userStates={$userStates} />

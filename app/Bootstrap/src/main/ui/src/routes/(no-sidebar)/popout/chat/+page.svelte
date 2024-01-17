<script>
	import ChatViewer from '$lib/chat/Chat.svelte';

	import { onMount } from 'svelte';

	let chatViewer;

	let userStates = {};

	function doAction(action, data) {
		switch (action) {
			case 'save-preferences': {
				Widget.emit('savePreferences', data);
				return;
			}
			case 'chat': {
				const { platform, message, replyTarget } = data;
				Koi.sendChat(platform, message, 'CLIENT', replyTarget, true);
				return;
			}
			case 'delete': {
				const { platform, eventId } = data;
				Koi.deleteChat(platform, eventId, true);
				return;
			}
			case 'upvote': {
				const { platform, eventId } = data;
				Koi.upvoteChat(platform, eventId);
				return;
			}
		}
	}

	onMount(() => {
		Koi.on('koi_statics', () => {
			userStates = Koi.userStates;
		});
		Koi.broadcast('koi_statics', {});

		for (const event of Koi.eventHistory) {
			chatViewer.processEvent({ ...event, is_historical: true });
		}

		chatViewer.supportedFeatures = Koi.features;

		// Kickstart.
		chatViewer.loadConfig(Widget.getSetting('preferences') || {});

		// Unsubscribe when this page exits.
		Koi.on('*', (_, event) => chatViewer.processEvent(event));
	});
</script>

<div class="-mx-6 -my-4 pb-2 h-screen">
	<ChatViewer bind:this={chatViewer} {doAction} {userStates} />
</div>

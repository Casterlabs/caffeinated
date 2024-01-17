<script>
	import ActivityFeedViewer from '$lib/chat/ActivityFeed.svelte';

	import { onMount } from 'svelte';

	let viewer;

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
			viewer.processEvent({ ...event, is_historical: true });
		}

		viewer.supportedFeatures = Koi.features;

		// Kickstart.
		viewer.loadConfig(Widget.getSetting('preferences') || {});

		// Unsubscribe when this page exits.
		Koi.on('*', (_, event) => viewer.processEvent(event));
	});
</script>

<div class="-mx-6 -my-4 h-screen">
	<ActivityFeedViewer bind:this={viewer} {doAction} {userStates} />
</div>

<script>
	import ActivityFeedViewer from '$lib/chat/ActivityFeed.svelte';

	import { onMount } from 'svelte';

	let viewer;

	const userStates = st || window.svelte('Caffeinated.koi', 'userStates');
	const supportedFeatures = st || window.svelte('Caffeinated.koi', 'features');

	function doAction(action, data) {
		switch (action) {
			case 'save-preferences': {
				Caffeinated.UI.activityPreferences = data;
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
				viewer.processEvent(event);
			}
		});

		// Kickstart.
		Caffeinated.UI.activityPreferences.then(viewer.loadConfig);

		// Unsubscribe when this page exits.
		const eventListener = window.saucer.messages.onMessage(([type, data]) => {
			if (type == 'koi:event') {
				viewer.processEvent(data);
			}
		});
		return () => {
			window.saucer.messages.off(eventListener);
		};
	});
</script>

<ActivityFeedViewer
	bind:this={viewer}
	{doAction}
	userStates={$userStates}
	supportedFeatures={$supportedFeatures || {}}
/>

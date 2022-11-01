<script>
	import PlatformMessage from './messages/PlatformMessage.svelte';
	import ChannelPointsMessage from './messages/ChannelPointsMessage.svelte';
	import SubscriptionMessage from './messages/SubscriptionMessage.svelte';
	import RichMessage from './messages/RichMessage.svelte';
	import ViewerLeaveMessage from './messages/ViewerLeaveMessage.svelte';
	import ViewerJoinMessage from './messages/ViewerJoinMessage.svelte';
	import ClearChatMessage from './messages/ClearChatMessage.svelte';
	import FollowMessage from './messages/FollowMessage.svelte';
	import RaidMessage from './messages/RaidMessage.svelte';

	import { createEventDispatcher } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('ChatViewer');
	const dispatch = createEventDispatcher();

	let chatBox;
	let chatElements = {};

	// prettier-ignore
	const EVENT_CLASSES = {
		PLATFORM_MESSAGE: PlatformMessage,
		CHANNEL_POINTS:   ChannelPointsMessage,
		SUBSCRIPTION:     SubscriptionMessage,
		RICH_MESSAGE:     RichMessage,
		VIEWER_LEAVE:     ViewerLeaveMessage,
		VIEWER_JOIN:      ViewerJoinMessage,
		CLEARCHAT:        ClearChatMessage,
		FOLLOW:           FollowMessage,
		RAID:             RaidMessage,
	};

	function onContextMenuAction(action, event) {}

	export function processEvent(event) {
		console.log('Processing event:', event);

		switch (event.event_type) {
			case 'VIEWER_LIST':
				break;

			case 'META': {
				const chatElement = chatElements[event.meta_id];

				if (chatElement) {
					chatElement.upvotes = event.upvotes;
					chatElement.isDeleted = !event.is_visible;
				}
				break;
			}

			case 'CLEARCHAT': {
				if (event.user_upid) {
					// Clear by user.
					for (const chatMessage of Object.values(chatElements)) {
						const koiEvent = chatMessage.koiEvent;

						if (koiEvent.sender && koiEvent.sender.UPID == event.user_upid) {
							chatMessage.isDeleted = true;
						}
					}
					break; // Do not fallthrough
				} else {
					// Clear all.
					const now = Date.now();

					for (const chatMessage of Object.values(chatElements)) {
						if (chatMessage.timestamp < now) {
							chatMessage.isDeleted = true;
						}
					}

					// Fallthrough
				}
			}

			default: {
				const clazz = EVENT_CLASSES[event.event_type];
				if (!clazz) return;

				const elem = document.createElement('li');
				const message = new clazz({
					target: elem,
					props: {
						event,
						onContextMenuAction
					}
				});

				if (event.meta_id) {
					// This event is editable in some way, shape, or form.
					// (so, we must keep track of it)
					chatElements[event.meta_id] = message;
				}

				chatBox.appendChild(elem);
			}
		}
	}
</script>

<div class="h-screen -mx-6 -my-4 flex flex-col">
	<div class="flex-1 overflow-x-hidden overflow-y-auto">
		<ul bind:this={chatBox} />
	</div>

	<div class="flex-0 h-16">...</div>
</div>

<style>
	ul :global(b) {
		font-weight: 600;
		color: var(--primary11);
	}
</style>

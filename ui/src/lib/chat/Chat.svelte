<svelte:options accessors />

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

	import InputBox from './InputBox.svelte';

	import createConsole from '../console-helper.mjs';
	const console = createConsole('ChatViewer');

	export let doAction = (action, data) => {};

	let chatBox;
	let chatElements = {};

	// Preferences
	let showChatTimestamps = true;
	let showModActions = true;
	let showProfilePictures = false;
	let showBadges = false;
	let showViewers = false;

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

	function onContextMenuAction(action, event) {
		console.debug('Context Menu Action:', action, event);

		function sendCommand(command) {
			doAction('chat', {
				platform: event.sender.platform,
				message: command,
				replyTarget: null
			});
		}

		switch (action) {
			case 'ban': {
				switch (event.sender.platform) {
					case 'TWITCH':
						sendCommand(`/ban ${event.sender.username}`);
						return;

					case 'TROVO':
						sendCommand(`/ban ${event.sender.username}`);
						return;

					default:
						return;
				}
			}

			case 'timeout': {
				// We timeout for 10 minutes
				switch (event.sender.platform) {
					case 'TWITCH':
						sendCommand(`/timeout ${event.sender.username} 600`);
						return;

					case 'TROVO':
						sendCommand(`/ban ${event.sender.username} 600`);
						return;

					default:
						return;
				}
			}

			case 'raid': {
				switch (event.sender.platform) {
					case 'CAFFEINE':
						sendCommand(`/afterparty @${event.sender.username}`);
						return;

					case 'TWITCH':
						sendCommand(`/raid ${event.sender.username}`);
						return;

					case 'TROVO':
						sendCommand(`/host ${event.sender.username}`);
						return;

					default:
						return;
				}
			}

			case 'delete': {
				doAction('delete', {
					platform: event.sender.platform,
					eventId: event.id
				});
				return;
			}

			case 'upvote': {
				doAction('upvote', {
					platform: event.sender.platform,
					eventId: event.id
				});
				return;
			}
		}
	}

	export function savePreferences() {
		const { x, y, width, height } = viewersList.getPositionData();

		doAction('save-preferences', {
			showChatTimestamps: showChatTimestamps,
			showModActions: showModActions,
			showProfilePictures: showProfilePictures,
			showBadges: showBadges,
			showViewers: showViewers,
			showViewersList: showViewersList,
			viewersX: x,
			viewersY: y,
			viewersWidth: width,
			viewersHeight: height
		});
	}

	export function loadConfig(config) {
		showChatTimestamps = config.showChatTimestamps;
		showModActions = config.showModActions;
		showProfilePictures = config.showProfilePictures;
		showBadges = config.showBadges;
		showViewers = config.showViewers;
	}

	export function processEvent(event) {
		console.log('Processing event:', event);

		switch (event.event_type) {
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

<div class="h-full px-2 py-1 flex flex-col">
	<div class="flex-1 overflow-x-hidden overflow-y-auto">
		<ul bind:this={chatBox} />
	</div>

	<div class="flex-0 pt-2 pb-1 h-fit">
		<InputBox />
	</div>
</div>

<style>
	ul :global(b) {
		font-weight: 600;
		color: var(--primary11);
	}
</style>

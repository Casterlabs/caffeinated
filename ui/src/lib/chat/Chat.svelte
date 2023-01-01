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
	import Modal from '$lib/ui/Modal.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Switch from '$lib/ui/Switch.svelte';

	import createConsole from '../console-helper.mjs';
	import { fade } from 'svelte/transition';
	const console = createConsole('ChatViewer');

	export let doAction = (action, data) => {};
	export let userStates = [];

	/** @type {HTMLElement} */
	let chatBox;
	let chatElements = {};

	// Preferences
	let settingsModalOpen = false;
	let showChatTimestamps = false;
	let showProfilePictures = false;
	let showBadges = false;
	let showBadgesOnLeft = false;
	let showViewers = false;

	let isAtBottom = true;

	function checkNearBottom() {
		const elem = chatBox.parentElement;
		const scrollPercent = (elem.scrollTop + elem.clientHeight) / elem.scrollHeight;
		isAtBottom = scrollPercent >= 0.9;
	}

	function jumpToBottom(behavior = 'smooth') {
		const elem = chatBox.parentElement;

		setTimeout(() => {
			elem.scrollTo({ top: elem.scrollHeight + 200, behavior });
		}, 50);
	}

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
		doAction('save-preferences', {
			showChatTimestamps: showChatTimestamps,
			showProfilePictures: showProfilePictures,
			showBadges: showBadges,
			showBadgesOnLeft: showBadgesOnLeft,
			showViewers: showViewers
		});
	}

	export function loadConfig(config) {
		showChatTimestamps = config.showChatTimestamps;
		showProfilePictures = config.showProfilePictures;
		showBadges = config.showBadges;
		showBadgesOnLeft = config.showBadgesOnLeft;
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
				if (!clazz) break;

				const messageTimestamp = document.createElement('span');
				messageTimestamp.classList = 'message-timestamp';
				messageTimestamp.innerText = new Date(event.timestamp || Date.now()).toLocaleTimeString();

				const messageContainer = document.createElement('span');
				messageContainer.classList = 'message-content';
				const message = new clazz({
					target: messageContainer,
					props: {
						event,
						onContextMenuAction
					}
				});

				const li = document.createElement('li');
				li.classList = 'message-container';
				li.appendChild(messageTimestamp);
				li.appendChild(messageContainer);

				if (['VIEWER_JOIN', 'VIEWER_LEAVE'].includes(event.event_type)) {
					li.classList.add('viewer-joinleave');
				}

				if (event.meta_id) {
					// This event is editable in some way, shape, or form.
					// (so, we must keep track of it)
					chatElements[event.meta_id] = message;
				}

				chatBox.appendChild(li);
				break;
			}
		}

		if (isAtBottom) {
			jumpToBottom('auto');
		}
	}
</script>

{#if settingsModalOpen}
	<Modal on:close={() => (settingsModalOpen = false)}>
		<LocalizedText slot="title" key="chat.viewer.preferences.title" />

		<ul class="w-72 divide-y divide-current text-base-6">
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.show_chat_timestamps"
					description=""
					bind:checked={showChatTimestamps}
					on:value={savePreferences}
				/>
			</li>
			<!-- <li class="py-2">
				<Switch
					title="chat.viewer.preferences.show_profile_pictures"
					description=""
					bind:checked={showProfilePictures}
					on:value={savePreferences}
				/>
			</li> -->
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.show_badges"
					description=""
					bind:checked={showBadges}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.show_viewers"
					description=""
					bind:checked={showViewers}
					on:value={savePreferences}
				/>
			</li>
		</ul>
	</Modal>
{/if}

<div
	class="h-full px-2 pt-2 flex flex-col relative"
	class:show-timestamps={showChatTimestamps}
	class:show-badges={showBadges}
	class:show-viewers={showViewers}
>
	<div class="flex-1 overflow-x-hidden overflow-y-auto" on:scroll={checkNearBottom}>
		<ul bind:this={chatBox} />
	</div>

	{#if !isAtBottom}
		<button
			class="absolute bottom-14 right-3 cursor-pointer rounded-full p-1 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			transition:fade
			on:click={() => jumpToBottom()}
		>
			<icon data-icon="icon/arrow-small-down" />
		</button>
	{/if}

	<div class="flex-0 pt-2 pb-1 h-fit">
		<InputBox
			{userStates}
			on:send={({ detail }) => {
				doAction('chat', {
					message: detail.message,
					platform: detail.platform,
					replyTarget: detail.replyTarget?.id || null
				});
			}}
		>
			<button
				class="absolute inset-y-2 right-2 translate-y-px"
				on:click={() => (settingsModalOpen = true)}
			>
				<icon
					class="w-5 h-5 transition-all"
					class:rotate-45={settingsModalOpen}
					data-icon="icon/cog"
				/>
			</button>
		</InputBox>
	</div>
</div>

<style>
	ul :global(b) {
		font-weight: 600;
		color: var(--primary11);
	}

	/* Timestamps */

	:global(.message-container) {
		display: flex;
		flex-direction: row;
		align-items: center;
	}

	:global(.message-timestamp) {
		display: none;
		user-select: none !important;
		font-weight: 625;
		font-size: 0.7em;
	}

	.show-timestamps :global(.message-content) {
		margin-left: 6px;
	}

	.show-timestamps :global(.message-timestamp) {
		display: inline-block;
	}

	/* Badges */

	:global(.richmessage-badges) {
		display: none;
	}

	.show-badges :global(.richmessage-badges) {
		display: unset !important;
	}

	/* Viewer Join/Leave Messages */

	:global(.viewer-joinleave) {
		display: none;
	}

	.show-viewers :global(.viewer-joinleave) {
		display: unset !important;
	}
</style>

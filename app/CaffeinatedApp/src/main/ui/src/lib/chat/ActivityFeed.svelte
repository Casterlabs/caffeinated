<svelte:options accessors />

<script>
	import ChannelPointsMessage from './messages/ChannelPointsMessage.svelte';
	import SubscriptionMessage from './messages/SubscriptionMessage.svelte';
	import FollowMessage from './messages/FollowMessage.svelte';
	import RaidMessage from './messages/RaidMessage.svelte';

	import Modal from '$lib/ui/Modal.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Switch from '$lib/ui/Switch.svelte';

	import createConsole from '../console-helper.mjs';
	import { fade } from 'svelte/transition';

	const console = createConsole('ChatViewer');

	export let doAction = (action, data) => {};
	export let supportedFeatures = {};

	/** @type {HTMLElement} */
	let chatBox;
	let chatElements = {};

	// Preferences
	let settingsModalOpen = false;

	let showTimestamps = false;
	let showProfilePictures = false;
	let showPlatform = false;
	let colorByPlatform = false;

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
		CHANNEL_POINTS:   ChannelPointsMessage,
		SUBSCRIPTION:     SubscriptionMessage,
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
			showTimestamps,
			showProfilePictures,
			showPlatform,
			colorByPlatform
		});
	}

	export function loadConfig(config) {
		showTimestamps = config.showChatTimestamps;
		showProfilePictures = config.showProfilePictures;
		showPlatform = config.showPlatform;
		colorByPlatform = config.colorByPlatform;
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
				return; // Avoid triggering the code below.
			}

			case 'MESSAGE_REACTION': {
				const chatElement = chatElements[event.meta_id];

				if (chatElement) {
					chatElement.reactions.push(event.reaction);
					chatElement.reactions = chatElement.reactions; // Re-render.
				}
				return; // Avoid triggering the code below.
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
				if (!clazz) return; // Avoid triggering the code below.

				const messageTimestamp = document.createElement('span');
				messageTimestamp.classList = 'message-timestamp';
				messageTimestamp.innerText = new Date(event.timestamp || Date.now()).toLocaleTimeString();

				const messageContainer = document.createElement('span');
				messageContainer.classList = 'message-content';
				const message = new clazz({
					target: messageContainer,
					props: {
						event,
						onContextMenuAction,
						supportedFeatures
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
					bind:checked={showTimestamps}
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
					title="chat.viewer.preferences.show_platform"
					description=""
					bind:checked={showPlatform}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.color_users_by_platform"
					description=""
					bind:checked={colorByPlatform}
					on:value={savePreferences}
				/>
			</li>
		</ul>
	</Modal>
{/if}

<div
	class="h-full px-2 pt-2 flex flex-col relative"
	class:show-timestamps={showTimestamps}
	class:show-platform={showPlatform}
	class:color-by-platform={colorByPlatform}
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

	<button
		class="absolute bottom-3.5 right-2.5 translate-y-px"
		on:click={() => (settingsModalOpen = true)}
	>
		<icon class="w-5 h-5 transition-all" class:rotate-45={settingsModalOpen} data-icon="icon/cog" />
	</button>
</div>

<style>
	ul :global(b) {
		font-weight: 600;
		color: var(--primary11);
	}

	.color-by-platform ul :global(b) {
		font-weight: 600;
		color: var(--platform-color, var(--primary11));
	}

	:global(.message-container) {
		display: block;
		white-space: nowrap;
		width: 100%;
		overflow: hidden;
	}

	:global(.message-content) {
		width: calc(100% - var(--timestamp-width, 0));
		display: inline-block;
		white-space: normal;
	}

	/* User Platform */
	:global(.user-platform) {
		display: none;
	}

	.show-platform :global(.user-platform) {
		display: inline-block !important;
	}

	/* Timestamps */

	:global(.message-timestamp) {
		display: none;
		user-select: none !important;
		font-weight: 625;
		font-size: 0.7em;
		vertical-align: top;
		text-align: right;
		transform: translateY(0.3rem);
	}

	.show-timestamps :global(.message-container) {
		--timestamp-width: 11ch;
	}

	.show-timestamps :global(.message-timestamp) {
		display: inline-block;
		width: var(--timestamp-width, 0);
		padding-right: 5px;
	}
</style>
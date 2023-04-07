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
	import SelectMenu from '$lib/ui/SelectMenu.svelte';

	import createConsole from '../console-helper.mjs';
	import { fade } from 'svelte/transition';
	import { SUPPORTED_TTS_VOICES } from '$lib/app.mjs';
	import { t } from '$lib/translate.mjs';
	import { onDestroy } from 'svelte';

	const console = createConsole('ChatViewer');

	export let doAction = (action, data) => {};
	export let userStates = [];
	export let supportedFeatures = {};

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
	let playDingOnMessage = false;
	let readMessagesAloud = false;
	let ttsVoice = 'Brian';
	let showPlatform = false;

	let isAtBottom = true;

	/** @type {HTMLAudioElement} */
	let ttsAudio = null;
	let ttsQueue = [];

	function checkTTSQueue() {
		if (ttsAudio) return;
		if (ttsQueue.length == 0) return;

		const message = ttsQueue.shift();

		ttsAudio = new Audio(
			`https://api.casterlabs.co/v1/polly?request=speech&voice=${ttsVoice}&text=${message}`
		);

		ttsAudio.addEventListener('ended', () => {
			ttsAudio = null;
			checkTTSQueue();
		});

		ttsAudio.play();
	}

	function skipTTS() {
		if (ttsAudio) {
			ttsAudio.pause();
			ttsAudio = null;
			checkTTSQueue();
		}
	}

	onDestroy(() => {
		ttsQueue = [];
		skipTTS();
	});

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
		if (!readMessagesAloud) {
			ttsQueue = [];
			skipTTS();
		}

		doAction('save-preferences', {
			showChatTimestamps,
			showProfilePictures,
			showBadges,
			showBadgesOnLeft,
			showViewers,
			playDingOnMessage,
			readMessagesAloud,
			ttsVoice,
			showPlatform
		});
	}

	export function loadConfig(config) {
		showChatTimestamps = config.showChatTimestamps;
		showProfilePictures = config.showProfilePictures;
		showBadges = config.showBadges;
		showBadgesOnLeft = config.showBadgesOnLeft;
		showViewers = config.showViewers;
		playDingOnMessage = config.playDingOnMessage;
		readMessagesAloud = config.readMessagesAloud;
		ttsVoice = config.ttsVoice;
		showPlatform = config.showPlatform;
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

		if (event.is_historical) {
			return;
		}

		if (playDingOnMessage) {
			new Audio('/sounds/dink.mp3').play();
		}

		if (readMessagesAloud) {
			if (event.event_type == 'RICH_MESSAGE') {
				let hasLink = false;
				let hasEmotesOrEmojis = false;
				let text = '';

				for (const fragment of event.fragments) {
					switch (fragment.type) {
						case 'TEXT':
							text += fragment.raw;
							break;

						case 'EMOTE':
							hasEmotesOrEmojis = true;
							text += fragment.emoteName;
							break;

						case 'EMOJI':
							hasEmotesOrEmojis = true;
							text += `${fragment.variation.identifier} emoji`;
							break;

						case 'LINK':
							hasLink = true;
							break;

						case 'MENTION':
							text += fragment.mentioned.displayname;
							break;
					}
					text += ' ';
				}

				text = text.trim();

				let format;

				if (event.fragments.length == 1 && hasLink) {
					format = 'SENT_A_LINK'; // Only one fragment and it's a link.
				} else if (text.length == 0) {
					if (hasEmotesOrEmojis) {
						format = 'SENT_SOME_EMOTES';
					}
				} else {
					// Try to guess whether or not the person was asking a question.
					if (text.endsWith('?')) {
						format = 'ASKS';
					} else {
						format = 'SAYS';
					}
				}

				if (format) {
					ttsQueue.push(
						t(`chat.viewer.tts.event.RICH_MESSAGE.${format}`, {
							name: event.sender.displayname,
							message: text
						})
					);
				}

				if (event.attachments.length > 0) {
					ttsQueue.push(
						t('chat.viewer.tts.event.RICH_MESSAGE.SENT_AN_ATTACHMENT', {
							name: event.sender.displayname
						})
					);
				}

				checkTTSQueue();
				return;
			}

			let message;

			switch (event.event_type) {
				case 'CHANNEL_POINTS':
					message = t('chat.viewer.tts.event.CHANNELPOINTS', {
						name: event.sender.displayname,
						reward: event.reward.title
					});
					break;

				case 'SUBSCRIPTION':
					message = t(`chat.viewer.tts.event.SUBSCRIPTION.${event.sub_type}`, {
						months: event.months,
						name: event.subscriber?.displayname,
						gifter: event.subscriber?.displayname,
						recipient: event.gift_recipient?.displayname
					});
					break;

				case 'FOLLOW':
					message = t('chat.viewer.tts.event.FOLLOW', {
						name: event.follower.displayname
					});
					break;

				case 'RAID':
					message = t('chat.viewer.tts.event.RAID', {
						name: event.host.displayname,
						viewers: event.viewers
					});
					break;
			}

			if (message) {
				ttsQueue.push(message);
				checkTTSQueue();
			}
		}
	}
</script>

{#if settingsModalOpen}
	<Modal on:close={() => (settingsModalOpen = false)}>
		<LocalizedText slot="title" key="chat.viewer.preferences.title" />

		<ul class="w-72 divide-y divide-current text-base-6">
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.play_ding_on_message"
					description=""
					bind:checked={playDingOnMessage}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.read_messages_out_loud"
					description=""
					bind:checked={readMessagesAloud}
					on:value={savePreferences}
				/>
			</li>
			{#if readMessagesAloud}
				<li class="py-2">
					<SelectMenu
						title="chat.viewer.preferences.tts_voice"
						description=""
						options={SUPPORTED_TTS_VOICES.reduce((arr, v) => ({ ...arr, [v]: v }), {})}
						bind:value={ttsVoice}
						on:value={savePreferences}
					/>
				</li>
			{/if}
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
			<li class="py-2">
				<Switch
					title="chat.viewer.preferences.show_platform"
					description=""
					bind:checked={showPlatform}
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
	class:show-platform={showPlatform}
>
	<div class="flex-1 overflow-x-hidden overflow-y-auto" on:scroll={checkNearBottom}>
		<ul bind:this={chatBox} />
	</div>

	{#if ttsAudio}
		<button
			class="absolute top-6 right-3 cursor-pointer rounded-full p-1 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			transition:fade
			on:click={skipTTS}
			title={t('chat.viewer.tts.skip')}
		>
			<icon class="h-8 w-8" data-icon="icon/forward" />
		</button>
	{/if}

	{#if !isAtBottom}
		<button
			class="absolute bottom-14 right-3 cursor-pointer rounded-full p-1 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			transition:fade
			on:click={jumpToBottom}
		>
			<icon data-icon="icon/arrow-small-down" />
		</button>
	{/if}

	<div class="flex-0 pt-2 pb-1 h-fit">
		<InputBox
			{userStates}
			{supportedFeatures}
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
		display: unset !important;
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
		display: block !important;
	}
</style>

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
	import User from './messages/User.svelte';

	import InputBox from './InputBox.svelte';
	import Modal from '$lib/ui/Modal.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Switch from '$lib/ui/Switch.svelte';
	import SelectMenu from '$lib/ui/SelectMenu.svelte';
	import RangeInput from '$lib/ui/RangeInput.svelte';

	import createConsole from '$lib/console-helper.mjs';
	import { fade } from 'svelte/transition';
	import { SUPPORTED_TTS_VOICES, openLink } from '$lib/app.mjs';
	import { t } from '$lib/app.mjs';
	import { onDestroy, tick } from 'svelte';

	const MAX_EVENTS_DISPLAY = 800;

	const console = createConsole('ChatViewer');

	export let doAction = (action, data) => {};
	export let userStates = [];
	export let supportedFeatures = {};

	/** @type {HTMLElement} */
	let scrollContainer = null;
	/** @type {HTMLElement} */
	let chatBox;
	let chatElements = {};
	let chatHistory = [];

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
	let showActivities = false;
	let showPronouns = false;
	let showZebraStripes = false;
	let colorBy = 'THEME';
	let ttsOrDingVolume;
	let textSize = 1;
	let inputBoxPreferences = {};

	let isAtBottom = true;

	/** @type {HTMLAudioElement} */
	let ttsAudio = null;
	let ttsQueue = [];

	let showUserModalFor = null;

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

		ttsAudio.volume = ttsOrDingVolume;
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
		isAtBottom = elem.scrollTop == 0; // Note that the scroll direction is inverted via CSS+JS.
	}

	function jumpToBottom(behavior = 'smooth') {
		const elem = chatBox.parentElement;

		setTimeout(() => {
			elem.scrollTo({
				top: 0, // Note that the scroll direction is inverted via CSS+JS.
				behavior
			});
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
			showPlatform,
			showActivities,
			colorBy,
			ttsOrDingVolume,
			textSize,
			showPronouns,
			showZebraStripes,
			inputBoxPreferences
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
		showActivities = config.showActivities;
		colorBy = config.colorBy;
		ttsOrDingVolume = config.ttsOrDingVolume;
		textSize = config.textSize;
		showPronouns = config.showPronouns;
		showZebraStripes = config.showZebraStripes;
		inputBoxPreferences = config.inputBoxPreferences;
	}

	function mountEvent(event, listElement) {
		const clazz = EVENT_CLASSES[event.event_type];
		if (!clazz) return { element: null, comp: null }; // Avoid triggering the code below.

		const messageTimestamp = document.createElement('span');
		messageTimestamp.classList = 'message-timestamp';
		messageTimestamp.innerText = new Date(event.timestamp || Date.now()).toLocaleTimeString();

		const messageContainer = document.createElement('span');
		messageContainer.classList = 'message-content';
		const comp = new clazz({
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

		if (
			event.attributes?.includes('HIGHLIGHTED') ||
			event.donations?.length > 0 ||
			['SUBSCRIPTION', 'RAID'].includes(event.event_type)
		) {
			li.classList.add('highlighted');
		}

		switch (event.event_type) {
			case 'VIEWER_JOIN':
			case 'VIEWER_LEAVE':
				li.classList.add('viewer-joinleave');
				break;

			case 'RICH_MESSAGE': {
				if (event.donations.length != 0) {
					li.classList.add('activity-event');
				}
				break;
			}

			case 'CHANNEL_POINTS':
			case 'SUBSCRIPTION':
			case 'FOLLOW':
			case 'RAID':
				li.classList.add('activity-event');
				break;
		}

		listElement.appendChild(li);
		return { element: li, comp };
	}

	export async function processEvent(event) {
		console.log('Processing event:', event);

		switch (event.event_type) {
			case 'META': {
				const chatElement = chatElements[event.meta_id];

				if (chatElement) {
					chatElement.component.upvotes = event.upvotes;
					chatElement.component.isDeleted = !event.is_visible;
				}
				return; // Avoid triggering the code below.
			}

			case 'MESSAGE_REACTION': {
				const chatElement = chatElements[event.meta_id];

				if (chatElement) {
					chatElement.component.reactions.push(event.reaction);
					chatElement.component.reactions = chatElement.reactions; // Re-render.
				}
				return; // Avoid triggering the code below.
			}

			case 'CLEARCHAT': {
				if (event.user_upid) {
					// Clear by user.
					for (const { component: chatMessage } of Object.values(chatElements)) {
						const koiEvent = chatMessage.koiEvent;

						if (koiEvent.sender && koiEvent.sender.UPID == event.user_upid) {
							chatMessage.isDeleted = true;
						}
					}
					break; // Do not fallthrough
				} else {
					// Clear all.
					const now = Date.now();

					for (const { component: chatMessage } of Object.values(chatElements)) {
						if (chatMessage.timestamp < now) {
							chatMessage.isDeleted = true;
						}
					}

					// Fallthrough
				}
			}

			default: {
				event.reply_target_data = chatElements[event.reply_target || ''] || null;

				const { element, comp } = mountEvent(event, chatBox);
				if (!element || !comp) return; // Avoid triggering the code below.

				if (event.meta_id) {
					// This event is editable in some way, shape, or form.
					// (so, we must keep track of it)
					chatElements[event.meta_id] = { element: element, component: comp, event: event };
				}

				chatHistory.push({ element: element, component: comp, event: event });

				if (chatHistory.length > MAX_EVENTS_DISPLAY) {
					const { element, event } = chatHistory.shift();
					element.remove();
					delete chatElements[event.meta_id];
				}

				const user =
					event.sender || event.follower || event.subscriber || event.host || event.viewer;
				if (user.UPID == showUserModalFor) {
					mountEvent(event, document.querySelector('#user-modal-list-element'));
				}

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
			switch (event.event_type) {
				case 'CHANNEL_POINTS':
				case 'RICH_MESSAGE':
				case 'SUBSCRIPTION':
				case 'FOLLOW':
				case 'RAID':
					const audio = new Audio('/$caffeinated-sdk-root$/sounds/dink.mp3');
					audio.volume = ttsOrDingVolume;
					audio.play();
					break;
			}
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
						await t(
							`co.casterlabs.caffeinated.app.docks.chat.viewer.tts.event_format.RICH_MESSAGE.${format}`,
							{
								name: event.sender.displayname,
								message: text
							}
						)
					);
				}

				if (event.attachments.length > 0) {
					ttsQueue.push(
						await t(
							'co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.tts.RICH_MESSAGE.ATTACHMENT',
							{
								name: event.sender.displayname
							}
						)
					);
				}

				checkTTSQueue();
				return;
			}

			let message;

			switch (event.event_type) {
				case 'CHANNEL_POINTS':
					message = await t(
						'co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.CHANNEL_POINTS',
						{
							name: event.sender.displayname,
							reward: event.reward.title
						}
					);
					break;

				case 'SUBSCRIPTION':
					message = await t(
						'co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.SUBSCRIPTION',
						{
							months_purchased: event.months_purchased,
							months_streak: event.months_streak,
							level: event.sub_level,
							type: event.sub_type,
							name: event.subscriber?.displayname,
							recipient: event.gift_recipient?.displayname
						}
					);
					break;

				case 'FOLLOW':
					message = await t('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.FOLLOW', {
						name: event.follower.displayname
					});
					break;

				case 'RAID':
					message = await t('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID', {
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
		<LocalizedText
			slot="title"
			key="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.title"
		/>

		<ul class="w-72 divide-y divide-current text-base-6">
			<li class="py-2">
				<SelectMenu
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by"
					description=""
					options={{
						THEME:
							'co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by.THEME',
						USER: 'co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by.USER',
						PLATFORM:
							'co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by.PLATFORM'
					}}
					bind:value={colorBy}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.play_ding_on_message"
					description=""
					bind:checked={playDingOnMessage}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.read_messages_out_loud"
					description=""
					bind:checked={readMessagesAloud}
					on:value={savePreferences}
				/>
			</li>
			{#if readMessagesAloud}
				<li class="py-2">
					<SelectMenu
						title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.tts_voice"
						description=""
						options={SUPPORTED_TTS_VOICES.reduce((arr, v) => ({ ...arr, [v]: v }), {})}
						bind:value={ttsVoice}
						on:value={savePreferences}
					/>
				</li>
			{/if}
			{#if readMessagesAloud || playDingOnMessage}
				<li class="py-2">
					<div class="w-full">
						<!-- svelte-ignore a11y-label-has-associated-control -->
						<label class="block text-sm font-medium text-base-12">
							<LocalizedText
								key="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.tts_or_ding_volume"
							/>
						</label>

						<RangeInput
							min={0}
							max={1}
							step={0.01}
							bind:value={ttsOrDingVolume}
							on:value={savePreferences}
						/>
					</div>
				</li>
			{/if}
			<li class="py-2">
				<div class="w-full">
					<!-- svelte-ignore a11y-label-has-associated-control -->
					<label class="block text-sm font-medium text-base-12">
						<LocalizedText
							key="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.text_size"
						/>
					</label>

					<RangeInput
						min={0.1}
						max={2}
						step={0.01}
						bind:value={textSize}
						on:value={savePreferences}
					/>
				</div>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_chat_timestamps"
					description=""
					bind:checked={showChatTimestamps}
					on:value={savePreferences}
				/>
			</li>
			<!-- <li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_profile_pictures"
					description=""
					bind:checked={showProfilePictures}
					on:value={savePreferences}
				/>
			</li> -->
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_badges"
					description=""
					bind:checked={showBadges}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_viewers"
					description=""
					bind:checked={showViewers}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_platform"
					description=""
					bind:checked={showPlatform}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_activities"
					description=""
					bind:checked={showActivities}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_pronouns"
					description=""
					bind:checked={showPronouns}
					on:value={savePreferences}
				/>
			</li>
			<li class="py-2">
				<Switch
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_zebra_stripes"
					description=""
					bind:checked={showZebraStripes}
					on:value={savePreferences}
				/>
			</li>
		</ul>
	</Modal>
{/if}

{#if showUserModalFor}
	<Modal on:close={() => (showUserModalFor = null)}>
		{@const events = chatHistory
			.filter(({ event }) => {
				const user =
					event.sender || event.follower || event.subscriber || event.host || event.viewer;
				return user?.UPID == showUserModalFor;
			})
			.map(({ event }) => event)}
		{@const userProfile = events.map(
			(event) => event.sender || event.follower || event.subscriber || event.host || event.viewer
		)[0]}

		<div
			class="text-base-12 w-screen max-w-md"
			class:show-timestamps={true}
			class:show-badges={true}
			class:show-viewers={true}
			class:show-platform={true}
			class:color-by-platform={colorBy == 'PLATFORM'}
			class:color-by-user={colorBy == 'USER'}
		>
			<!-- svelte-ignore a11y-no-noninteractive-element-interactions -->
			<!-- svelte-ignore a11y-click-events-have-key-events -->
			<h1
				class="text-lg mb-2 -mt-4"
				on:click={(e) => {
					if (e.target?.getAttribute('data-user-modal-for')) {
						openLink(userProfile.link);
					}
				}}
			>
				<LocalizedText
					key="co.casterlabs.caffeinated.app.docks.chat.viewer.user_history.title"
					slotMapping={['user']}
				>
					<User slot="0" user={userProfile} />
				</LocalizedText>
			</h1>

			<ul
				class="overflow-y-auto h-96"
				style="font-size: {textSize * 100}%;"
				id="user-modal-list-element"
			></ul>
			{(() => {
				tick().then(() => {
					for (const event of events) {
						mountEvent(event, document.querySelector('#user-modal-list-element'));
					}
					document.querySelector('#user-modal-list-element').scrollTop = document.querySelector(
						'#user-modal-list-element'
					).scrollHeight;
				});
				return '';
			})()}
		</div>
	</Modal>
{/if}

<div
	class="h-full px-2 pt-2 flex flex-col relative overflow-hidden"
	class:show-timestamps={showChatTimestamps}
	class:show-badges={showBadges}
	class:show-viewers={showViewers}
	class:show-platform={showPlatform}
	class:show-activities={showActivities}
	class:show-pronouns={showPronouns}
	class:show-zebra-stripes={showZebraStripes}
	class:color-by-platform={colorBy == 'PLATFORM'}
	class:color-by-user={colorBy == 'USER'}
>
	<div
		class="flex-1 overflow-x-hidden overflow-y-auto"
		on:scroll={checkNearBottom}
		bind:this={scrollContainer}
		style="transform: scaleY(-1);"
		on:scroll|preventDefault
		on:wheel|preventDefault={(e) => {
			scrollContainer.scrollTop -= e.deltaY / 2;
		}}
	>
		<!-- svelte-ignore a11y-click-events-have-key-events -->
		<!-- svelte-ignore a11y-no-noninteractive-element-interactions -->
		<ul
			class="h-fit"
			style="transform: scaleY(-1); font-size: {(textSize || 1) * 100}%;"
			bind:this={chatBox}
			on:click={(e) => {
				if (e.target?.getAttribute('data-user-modal-for')) {
					e.preventDefault();
					showUserModalFor = e.target?.getAttribute('data-user-modal-for');
				}
			}}
		/>
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
			class="absolute bottom-14 left-2 right-2 opacity-95 cursor-pointer flex items-center justify-center rounded-md p-1 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			transition:fade
			on:click={() => jumpToBottom()}
		>
			<icon class="inline-block pr-1" data-icon="icon/pause" />
			<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.scrolling_paused" />
		</button>
	{/if}

	<div class="flex-0 pt-2 pb-1 h-fit">
		<InputBox
			{userStates}
			{supportedFeatures}
			preferences={inputBoxPreferences}
			on:send={({ detail }) => {
				doAction('chat', {
					message: detail.message,
					platform: detail.platform,
					replyTarget: detail.replyTarget?.id || null
				});
			}}
			on:save={({ detail }) => {
				inputBoxPreferences = detail;
				savePreferences();
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

	ul :global(li) {
		border-radius: 0.375rem; /* rounded-md */
	}

	/* Colors */
	.color-by-platform ul :global(b) {
		font-weight: 600;
		color: var(--platform-color, var(--primary11));
	}

	.color-by-user ul :global(b) {
		font-weight: 600;
		color: var(--user-color, var(--primary11));
	}

	/* User Platform */
	:global(.user-platform) {
		display: none;
	}

	.show-platform :global(.user-platform) {
		display: inline-block !important;
	}

	/* Activities */
	:global(.activity-event) {
		display: none !important;
	}

	.show-activities :global(.activity-event) {
		display: block !important;
	}

	/* Highlight */
	:global(li.highlighted) {
		padding: 0.5rem; /* p-2 */
		margin: 0.125rem 0; /* my-0.5 */
		background-color: var(--primary5); /* bg-primary-5 */
		text-shadow: 1px 1px 5px var(--primary3); /* Try to make the text more readable */
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
		display: none !important;
	}

	.show-viewers :global(.viewer-joinleave) {
		display: block !important;
	}

	/* Pronouns */
	:global(.pronouns) {
		display: none;
	}

	.show-pronouns :global(.pronouns) {
		display: unset !important;
	}

	/* Zebra Stripes */
	.show-zebra-stripes ul > :global(li):nth-child(odd) {
		background-color: var(--base4);
	}
</style>

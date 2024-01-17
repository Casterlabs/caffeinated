<svelte:options accessors />

<script>
	import ChannelPointsMessage from './messages/ChannelPointsMessage.svelte';
	import SubscriptionMessage from './messages/SubscriptionMessage.svelte';
	import RichMessage from './messages/RichMessage.svelte';
	import FollowMessage from './messages/FollowMessage.svelte';
	import RaidMessage from './messages/RaidMessage.svelte';

	import Modal from '$lib/ui/Modal.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Switch from '$lib/ui/Switch.svelte';
	import RangeInput from '$lib/ui/RangeInput.svelte';

	import createConsole from '$lib/console-helper.mjs';
	import { fade } from 'svelte/transition';
	import SelectMenu from '$lib/ui/SelectMenu.svelte';

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
	let showPronouns = false;
	let showZebraStripes = false;
	let textSize = 1;
	let colorBy = 'THEME';

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
		RICH_MESSAGE:     RichMessage,
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
			showPronouns,
			showZebraStripes,
			colorBy
		});
	}

	export function loadConfig(config) {
		showTimestamps = config.showChatTimestamps;
		showProfilePictures = config.showProfilePictures;
		showPlatform = config.showPlatform;
		colorBy = config.colorBy;
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

				if (event.event_type == 'RICH_MESSAGE' && event.donations.length == 0) {
					return; // Not an activity.
				}

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
		<LocalizedText
			slot="title"
			key="co.casterlabs.caffeinated.app.docks.activity_feed.preferences.title"
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
					bind:checked={showTimestamps}
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
					title="co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_platform"
					description=""
					bind:checked={showPlatform}
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

<div
	class="h-full px-2 pt-2 flex flex-col relative"
	class:show-timestamps={showTimestamps}
	class:show-platform={showPlatform}
	class:show-pronouns={showPronouns}
	class:show-zebra-stripes={showZebraStripes}
	class:color-by-platform={colorBy == 'PLATFORM'}
	class:color-by-user={colorBy == 'USER'}
>
	<div class="flex-1 overflow-x-hidden overflow-y-auto" on:scroll={checkNearBottom}>
		<ul style="font-size: {(textSize || 1) * 100}%;" bind:this={chatBox} />
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

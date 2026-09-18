<script lang="ts">
	import { Koi, getDockPreferences, saveDockPreferences } from '$lib/app-shim';
	import EventHandler from '$lib/event-handler';
	import type { KoiEvent, KoiEventType, RichMessageEvent } from '$lib/koi';
	import { render } from '$lib/locale/locale';
	import { fade } from 'svelte/transition';

	import ChatInput from '$lib/layout/chat/ChatInput.svelte';
	import EventListRenderer from '$lib/layout/chat/EventListRenderer.svelte';
	import ChatInputReply from '$lib/layout/modals/ChatInputReply.svelte';
	import ChatViewerSettings from '$lib/layout/modals/ChatViewerSettings.svelte';
	import EventListMessageModal from '$lib/layout/modals/EventListMessageModal.svelte';
	import SendTargetModal from '$lib/layout/modals/SendTargetModal.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconForward } from '@casterlabs/heroicons-svelte';

	import { onDestroy, onMount } from 'svelte';

	type PrefsType = Awaited<(typeof AppConfig)['uiPreferences']>['chatViewerPreferences'];

	const uiEvents = new EventHandler();

	let eventListRenderer: EventListRenderer;
	let prefs: PrefsType | null = $state(null);

	let ttsAudio: HTMLAudioElement | null = $state(null);
	let ttsQueue: string[] = [];

	// @ts-ignore
	let ttsOrDingVolume = $derived(Math.min(1, Math.max(0, prefs?.ttsOrDingVolume ?? 1)));
	// @ts-ignore
	let playDingOnMessage = $derived(prefs?.playDingOnMessage ?? false);
	// @ts-ignore
	let ttsVoice = $derived(prefs?.ttsVoice || '');
	// @ts-ignore
	let readMessagesAloud = $derived(prefs?.readMessagesAloud ?? false);

	function checkTTSQueue() {
		if (ttsAudio) return; // Already playing

		const message = ttsQueue.shift();
		if (!message) return; // Empty queue

		ttsAudio = new Audio(`https://api.casterlabs.co/v1/polly?request=speech&voice=${ttsVoice}&text=${encodeURIComponent(message)}`);

		function next() {
			ttsAudio = null;
			checkTTSQueue();
		}

		ttsAudio.addEventListener('ended', next);
		ttsAudio.addEventListener('error', next);

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

	function clearTTSQueue() {
		ttsQueue = [];
		skipTTS();
	}

	onDestroy(clearTTSQueue);

	onMount(async () => {
		prefs = await getDockPreferences<PrefsType>('chat');
		eventListRenderer.recomputeEvens(true);
	});

	onMount(() =>
		Koi.on('*', (_: string, event: KoiEvent) => {
			if (playDingOnMessage && ['FOLLOW', 'SUBSCRIPTION', 'RAID', 'CHANNEL_POINTS', 'RICH_MESSAGE', 'PURCHASE'].includes(event.event_type)) {
				const audio = new Audio('/$caffeinated-sdk-root$/sounds/dink.mp3');
				audio.volume = ttsOrDingVolume;
				audio.play();
			}

			if (!readMessagesAloud) return;

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

				if (format && text.length > 0) {
					ttsQueue.push(
						render(`co.casterlabs.caffeinated.app.docks.chat.viewer.tts.event_format.RICH_MESSAGE.${format}`, {
							name: event.sender.displayname,
							message: text
						})
					);
				}

				if (event.attachments.length > 0) {
					ttsQueue.push(
						render('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.tts.RICH_MESSAGE.ATTACHMENT', {
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
					message = render('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.CHANNEL_POINTS', {
						name: event.sender.displayname,
						reward: event.reward.title
					});
					break;

				case 'SUBSCRIPTION':
					message = render('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.SUBSCRIPTION', {
						months_purchased: event.months_purchased.toString(),
						months_streak: event.months_streak.toString(),
						level: event.sub_level,
						type: event.sub_type,
						name: event.subscriber?.displayname,
						recipient: event.gift_recipient?.displayname
					});
					break;

				case 'FOLLOW':
					message = render('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.FOLLOW', {
						name: event.follower.displayname
					});
					break;

				case 'RAID':
					message = render('co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID', {
						name: event.host.displayname,
						viewers: event.viewers.toString()
					});
					break;
			}

			if (message) {
				ttsQueue.push(message);
				checkTTSQueue();
			}
		})
	);

	let eventModal: KoiEvent | null = $state(null);
	onMount(() => uiEvents.on('x-event-modal', (e: KoiEvent) => (eventModal = e)));

	let replyModal: RichMessageEvent | null = $state(null);
	onMount(() => uiEvents.on('x-reply-modal', (e: RichMessageEvent) => (replyModal = e)));

	let sendTargetModal = $state(false);
	onMount(() => uiEvents.on('x-sendtarget-modal', () => (sendTargetModal = true)));

	let preferencesModal = $state(false);
	onMount(() => uiEvents.on('x-preferences-modal', () => (preferencesModal = true)));
</script>

<div
	class="h-full max-h-full flex flex-col event-renderer chat-renderer"
	class:HIDE-TIMESTAMP={!prefs?.showTimestamps}
	class:HIDE-PROFILEPICTURES={!prefs?.showProfilePictures}
	class:HIDE-BADGES={!prefs?.showBadges}
	class:HIDE-PLATFORM={!prefs?.showPlatform}
	class:HIDE-PRONOUNS={!prefs?.showPronouns}
	class:HIDE-ACTIVITIES={!prefs?.showActivities}
	class:HIDE-VIEWERS={!prefs?.showViewers}
	class:ZEBRA-STRIPES={prefs?.showZebraStripes}
	class:USERNAME-THEME={prefs?.colorBy == 'THEME'}
	class:USERNAME-USER={prefs?.colorBy == 'USER'}
	class:USERNAME-PLATFORM={prefs?.colorBy == 'PLATFORM'}
>
	<div class="flex-1 pb-1 overflow-hidden relative" style:font-size="{prefs?.textSize ?? 1}em">
		<EventListRenderer bind:this={eventListRenderer} {uiEvents} />
	</div>

	<div class="pt-2 px-1.5 pb-1 border border-transparent border-t-base-2">
		<ChatInput {uiEvents} />
	</div>
</div>

{#if ttsAudio}
	<button
		in:fade
		out:fade
		class="bg-base-3 border-base-7 absolute inset-x-2 top-2 flex items-center justify-center rounded-md border p-1 opacity-90 space-x-1 text-sm"
		onclick={skipTTS}
	>
		<span>
			<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.tts.skip" />
		</span>
		<IconForward theme="micro" />
	</button>
{/if}

{#if eventModal}
	<EventListMessageModal event={eventModal} {uiEvents} onclose={() => (eventModal = null)} />
{/if}

{#if replyModal}
	<ChatInputReply event={replyModal} {uiEvents} onclose={() => (replyModal = null)} />
{/if}

{#if sendTargetModal}
	<SendTargetModal {uiEvents} onclose={() => (sendTargetModal = false)} />
{/if}

{#if preferencesModal}
	<ChatViewerSettings
		initialPrefs={prefs!}
		onupdate={(newPrefs) => {
			eventListRenderer.recomputeEvens(true);
			prefs = newPrefs;

			if (!newPrefs.readMessagesAloud) {
				clearTTSQueue();
			}

			saveDockPreferences('chat', newPrefs);
		}}
		onclose={() => (preferencesModal = false)}
	/>
{/if}

<style>
	/* ---- Regular---- */

	:global(.chat-renderer.ZEBRA-STRIPES .even-child .event-renderer) {
		background-color: var(--base2);
	}

	:global(.chat-renderer .event-renderer:not(.er-donotmoderate):active),
	:global(.chat-renderer .event-renderer:not(.er-donotmoderate):hover) {
		background-color: var(--base2);
	}

	/* ---- Highlighted ---- */

	:global(.chat-renderer .event-renderer.er-highlighted) {
		background-color: var(--primary3);
	}

	:global(.chat-renderer.ZEBRA-STRIPES .even-child .event-renderer.er-highlighted) {
		background-color: var(--primary4);
	}

	:global(.chat-renderer .event-renderer.er-highlighted:not(.er-donotmoderate):active),
	:global(.chat-renderer .event-renderer.er-highlighted:not(.er-donotmoderate):hover) {
		background-color: var(--primary5);
	}
</style>

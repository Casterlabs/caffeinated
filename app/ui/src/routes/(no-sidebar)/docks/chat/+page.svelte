<script lang="ts">
	import { getDockPreferences, saveDockPreferences } from '$lib/app-shim';
	import EventHandler from '$lib/event-handler';
	import type { KoiEvent, RichMessageEvent } from '$lib/koi';

	import ChatInput from '$lib/layout/chat/ChatInput.svelte';
	import EventListRenderer from '$lib/layout/chat/EventListRenderer.svelte';
	import ChatInputReply from '$lib/layout/modals/ChatInputReply.svelte';
	import ChatViewerSettings from '$lib/layout/modals/ChatViewerSettings.svelte';
	import EventListMessageModal from '$lib/layout/modals/EventListMessageModal.svelte';
	import SendTargetModal from '$lib/layout/modals/SendTargetModal.svelte';

	import { onMount } from 'svelte';

	type PrefsType = Awaited<(typeof AppConfig)['uiPreferences']>['chatViewerPreferences'];

	const uiEvents = new EventHandler();

	let eventListRenderer: EventListRenderer;
	let prefs: PrefsType | null = $state(null);

	onMount(async () => {
		prefs = await getDockPreferences<PrefsType>('chat');
		eventListRenderer.recomputeEvens(true);
	});

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
	class="h-full max-h-full flex flex-col event-renderer"
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
	<div class="flex-1 pb-1 overflow-hidden relative">
		<EventListRenderer bind:this={eventListRenderer} {uiEvents} />
	</div>

	<div class="pt-2 px-1.5 pb-1 border border-transparent border-t-base-2">
		<ChatInput {uiEvents} />
	</div>
</div>

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
			saveDockPreferences('chat', newPrefs);
		}}
		onclose={() => (preferencesModal = false)}
	/>
{/if}

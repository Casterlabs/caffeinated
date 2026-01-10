<script lang="ts">
	import { getDockPreferences, saveDockPreferences } from '$lib/app-shim';
	import EventHandler from '$lib/event-handler';
	import type { KoiEvent } from '$lib/koi';

	import EventListRenderer from '$lib/layout/chat/EventListRenderer.svelte';
	import ActivityViewerSettings from '$lib/layout/modals/ActivityViewerSettings.svelte';
	import EventListMessageModal from '$lib/layout/modals/EventListMessageModal.svelte';

	import { onMount } from 'svelte';

	type PrefsType = Awaited<(typeof AppConfig)['uiPreferences']>['activityViewerPreferences'];

	const uiEvents = new EventHandler();

	let eventListRenderer: EventListRenderer;
	let prefs: PrefsType | null = $state(null);

	onMount(async () => {
		prefs = await getDockPreferences<PrefsType>('activity');
		eventListRenderer.recomputeEvens(true);
	});

	let eventModal: KoiEvent | null = $state(null);
	onMount(() => uiEvents.on('x-event-modal', (e: KoiEvent) => (eventModal = e)));

	let preferencesModal = $state(false);
	onMount(() => uiEvents.on('x-preferences-modal', () => (preferencesModal = true)));
</script>

<div
	class="h-full max-h-full flex flex-col event-renderer HIDE-VIEWERS HIDE-CHAT"
	class:HIDE-TIMESTAMP={!prefs?.showTimestamps}
	class:HIDE-PROFILEPICTURES={!prefs?.showProfilePictures}
	class:HIDE-BADGES={!prefs?.showBadges}
	class:HIDE-PLATFORM={!prefs?.showPlatform}
	class:HIDE-PRONOUNS={!prefs?.showPronouns}
	class:ZEBRA-STRIPES={prefs?.showZebraStripes}
	class:USERNAME-THEME={prefs?.colorBy == 'THEME'}
	class:USERNAME-USER={prefs?.colorBy == 'USER'}
	class:USERNAME-PLATFORM={prefs?.colorBy == 'PLATFORM'}
>
	<div class="flex-1 pb-1 overflow-hidden">
		<EventListRenderer bind:this={eventListRenderer} {uiEvents} />
	</div>
</div>

{#if eventModal}
	<EventListMessageModal event={eventModal} {uiEvents} onclose={() => (eventModal = null)} showReply={false} />
{/if}

{#if preferencesModal}
	<ActivityViewerSettings
		initialPrefs={prefs!}
		onupdate={(newPrefs) => {
			eventListRenderer.recomputeEvens(true);
			prefs = newPrefs;
			saveDockPreferences('activity', newPrefs);
		}}
		onclose={() => (preferencesModal = false)}
	/>
{/if}

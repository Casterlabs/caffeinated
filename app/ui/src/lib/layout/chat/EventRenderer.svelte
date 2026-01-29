<script module>
	import ChannelPointsRenderer from './events/ChannelPointsRenderer.svelte';
	import ClearchatRenderer from './events/ClearchatRenderer.svelte';
	import FollowRenderer from './events/FollowRenderer.svelte';
	import RaidRenderer from './events/RaidRenderer.svelte';
	import RichMessageRenderer from './events/RichMessageRenderer.svelte';
	import SubscriptionRenderer from './events/SubscriptionRenderer.svelte';

	const EVENT_COMPONENTS: Record<string, Component<any, any, any>> = {
		CLEARCHAT: ClearchatRenderer,
		CHANNEL_POINTS: ChannelPointsRenderer,
		FOLLOW: FollowRenderer,
		RAID: RaidRenderer,
		RICH_MESSAGE: RichMessageRenderer,
		PLATFORM_MESSAGE: RichMessageRenderer,
		SUBSCRIPTION: SubscriptionRenderer,
		VIEWER_JOIN: ViewerJoinRenderer,
		VIEWER_LEAVE: ViewerLeaveRenderer
	};

	export const SUPPORTED_EVENTS = Object.keys(EVENT_COMPONENTS);

	const ACTIVITY_EVENTS = ['CLEARCHAT', 'CHANNEL_POINTS', 'FOLLOW', 'RAID', 'SUBSCRIPTION'];
	const VIEWER_EVENTS = ['VIEWER_JOIN', 'VIEWER_LEAVE'];
</script>

<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';
	import type { ClearChatEvent, KoiEvent, MessageMetaEvent, MetaId, User } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import ViewerJoinRenderer from './events/ViewerJoinRenderer.svelte';
	import ViewerLeaveRenderer from './events/ViewerLeaveRenderer.svelte';
	import { LongPressListener } from '@casterlabs/ui';

	import { type Component, onMount } from 'svelte';

	const NO_MODERATE = ['CLEARCHAT', 'PLATFORM_MESSAGE'];

	interface Props {
		event: KoiEvent;
		uiEvents: EventHandler;
	}

	let { event, uiEvents }: Props = $props();

	const EventRenderer = EVENT_COMPONENTS[event.event_type];

	let isDeleted = $state((event as any).is_visible === false || event.x_cleared);
	let showAnyways = $state(false);

	// prettier-ignore
	let eventUser: User = $derived((event as any).sender || (event as any).follower || (event as any).subscriber || (event as any).host);
	let eventMetaId: MetaId = $derived((event as any).meta_id || null);

	let timestamp = $state(getTimestamp());

	onMount(() => {
		if (event.event_type == 'PLATFORM_MESSAGE') return; // Don't allow platform messages to be cleared.

		return Koi.on('CLEARCHAT', (cce: ClearChatEvent) => {
			if (eventMetaId) {
				// This means we're a message event, so we should mark ourselves as deleted for a clear type of ALL and for a USER ban.
				if (cce.clear_type == 'ALL' || cce.user_upid == eventUser.UPID) {
					isDeleted = true;
					event.x_cleared = true; // Mutate the object.
				}
			} else {
				// This means we're NOT a message event, so we should ONLY be deleted if the USER gets banned.
				if (cce.clear_type == 'USER' && cce.user_upid == eventUser.UPID) {
					isDeleted = true;
					event.x_cleared = true; // Mutate the object.
				}
			}
		});
	});

	onMount(() => {
		if (event.event_type == 'PLATFORM_MESSAGE') return; // Platform messages don't have META events.
		if (!eventMetaId) return; // Not a meta type, thus we don't need to listen.

		return Koi.on('META', (newMeta: MessageMetaEvent) => {
			if (newMeta.meta_id == eventMetaId) {
				isDeleted = !newMeta.is_visible;
			}
		});
	});

	function getTimestamp() {
		const timestamp = new Date(event.timestamp || Date.now());
		const now = new Date();
		if (
			//
			timestamp.getDate() === now.getDate() &&
			timestamp.getMonth() === now.getMonth() &&
			timestamp.getFullYear() === now.getFullYear()
		) {
			return timestamp.toLocaleTimeString();
		} else {
			return timestamp.toLocaleString();
		}
	}

	onMount(() => {
		const id = setInterval(
			() => {
				timestamp = getTimestamp();
			},
			2 /*m*/ * 60 * 1000
		);
		return () => clearInterval(id);
	});
</script>

{#if EventRenderer}
	{@const doNotModerate = NO_MODERATE.includes(event.event_type)}

	<LongPressListener onlongpress={() => uiEvents.broadcast('x-event-modal', event)}>
		<div
			class="event-renderer mt-0.5 py-1 break-anywhere relative px-2 will-change-transform"
			class:er-chat-event={!ACTIVITY_EVENTS.includes(event.event_type) && !VIEWER_EVENTS.includes(event.event_type)}
			class:er-activity-event={ACTIVITY_EVENTS.includes(event.event_type)}
			class:er-viewer-event={VIEWER_EVENTS.includes(event.event_type)}
			class:hover:bg-base-2={!doNotModerate}
			class:active:bg-base-2={!doNotModerate}
			class:text-base-11={isDeleted}
		>
			{#if isDeleted && !showAnyways}
				<span class="text-[0.75rem] text-base-11">
					<span class="er-timestamp text-[0.75rem] text-base-11 mr-0.5">
						{timestamp}
					</span>

					<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.deleted" />
					<button class="text-primary-10" onclick={() => (showAnyways = true)}>
						<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.deleted.show" />
					</button>
				</span>
			{:else}
				<!-- svelte-ignore a11y_no_static_element_interactions -->
				<div
					class="block w-full text-left"
					oncontextmenu={(e) => {
						e.preventDefault();
						uiEvents.broadcast('x-event-modal', event);
					}}
				>
					<span class="er-timestamp text-[0.75rem] text-base-11 mr-0.5">
						{timestamp}
					</span>

					<EventRenderer {event} {uiEvents} />
				</div>

				{#if isDeleted}
					<span class="text-[0.75rem] text-base-11">
						<button class="text-primary-10" onclick={() => (showAnyways = false)}>
							<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.deleted.hide" />
						</button>
					</span>
				{/if}
			{/if}
		</div>
	</LongPressListener>
{/if}

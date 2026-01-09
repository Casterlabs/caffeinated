<script lang="ts">
	import { Koi, openLink } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';
	import { type ClearChatEvent, HAS_BAN, HAS_DELETE, HAS_TIMEOUT, type KoiEvent, type MessageId, type MessageMetaEvent, type MetaId, type User } from '$lib/koi';

	import Modal from '../Modal.svelte';
	import { IconArrowTopRightOnSquare, IconArrowUturnLeft, IconNoSymbol, IconShieldExclamation, IconTrash } from '@casterlabs/heroicons-svelte';

	import { onMount } from 'svelte';

	interface Props {
		event: KoiEvent;
		uiEvents: EventHandler;
		onclose: () => void;
	}

	let { event: targetEvent, uiEvents, onclose }: Props = $props();

	let eventId: MessageId | null = $derived((targetEvent as any).id || null);
	let eventMetaId: MetaId | null = $derived((targetEvent as any).meta_id || null);
	let targetUser: User = $derived((targetEvent as any)?.sender || (targetEvent as any)?.follower || (targetEvent as any)?.subscriber || (targetEvent as any)?.host);

	let isDeleted = $state(false);

	$effect(() => {
		if (!targetEvent) return;
		isDeleted = !((targetEvent as any)?.is_visible ?? true);
	});

	onMount(() => {
		return Koi.on('CLEARCHAT', (e: ClearChatEvent) => {
			if (eventMetaId) {
				// This means we're a message event, so we should mark ourselves as deleted for a clear type of ALL and for a USER ban.
				if (e.clear_type == 'ALL' || e.user_upid == targetUser.UPID) {
					isDeleted = true;
				}
			} else {
				// This means we're NOT a message event, so we should ONLY be deleted if the USER gets banned.
				if (e.clear_type == 'USER' && e.user_upid == targetUser.UPID) {
					isDeleted = true;
				}
			}
		});
	});

	onMount(() => {
		return Koi.on('META', (newMeta: MessageMetaEvent) => {
			if (newMeta.meta_id == eventMetaId) {
				isDeleted = !newMeta.is_visible;
			}
		});
	});
</script>

{#snippet actionButton(title: string, Icon: any, onclick: () => void)}
	<button class="basis-full flex-grow-1 hover:bg-base-2 active:bg-base-2 py-4 rounded-lg flex flex-col items-center justify-center space-y-2" ontouchend={onclick} {onclick}>
		<Icon theme="outline" />
		<span class="text-xs"> {title} </span>
	</button>
{/snippet}

{#snippet title()}
	<div class="flex items-center justify-center">
		{targetUser.username}'s message
	</div>
{/snippet}

<Modal {title} {onclose}>
	<div class="flex items-center justify-stretch w-full">
		{#if targetEvent.event_type == 'RICH_MESSAGE' && !targetEvent.x_cleared && targetEvent.is_visible}
			{@render actionButton('Reply', IconArrowUturnLeft, () => {
				uiEvents.broadcast('x-start-reply', targetEvent);
				onclose();
			})}
		{/if}

		{#if HAS_DELETE.includes(targetEvent.streamer.platform) && eventId && !isDeleted}
			{@render actionButton('Delete', IconTrash, () => {
				if (!targetEvent) return;
				Koi.deleteChat(targetEvent.streamer.UPID, eventId);
			})}
		{/if}

		{#if HAS_BAN.includes(targetEvent.streamer.platform)}
			{@render actionButton('Ban', IconNoSymbol, () => {
				if (!targetEvent) return;
				Koi.banChatter(targetEvent.streamer.UPID, targetUser);
			})}
		{/if}

		{#if HAS_TIMEOUT.includes(targetEvent.streamer.platform)}
			{@render actionButton('Timeout', IconShieldExclamation, () => {
				if (!targetEvent) return;
				Koi.timeoutChatter(targetEvent.streamer.UPID, targetUser);
			})}
		{/if}

		{@render actionButton('Open', IconArrowTopRightOnSquare, () => {
			openLink(targetUser.link);
		})}
	</div>
</Modal>

<script lang="ts">
	import { eventListener } from '$lib/dom';
	import type { KoiEvent, RichMessageEvent } from '$lib/koi';

	import ChatInput from '$lib/layout/chat/ChatInput.svelte';
	import EventListRenderer from '$lib/layout/chat/EventListRenderer.svelte';
	import ChatInputReply from '$lib/layout/modals/ChatInputReply.svelte';
	import EventListMessageModal from '$lib/layout/modals/EventListMessageModal.svelte';
	import SendTargetModal from '$lib/layout/modals/SendTargetModal.svelte';

	import { onMount } from 'svelte';

	let eventModal: KoiEvent | null = $state(null);
	onMount(eventListener('x-event-modal', (e: KoiEvent) => (eventModal = e)));

	let replyModal: RichMessageEvent | null = $state(null);
	onMount(eventListener('x-reply-modal', (e: RichMessageEvent) => (replyModal = e)));

	let sendTargetModal = $state(false);
	onMount(eventListener('x-sendtarget-modal', () => (sendTargetModal = true)));
</script>

<div class="h-full max-h-full flex flex-col">
	<div class="flex-1 pb-1 overflow-hidden">
		<EventListRenderer />
	</div>

	<div class="pt-2 px-1.5 pb-1 border border-transparent border-t-base-2">
		<ChatInput />
	</div>
</div>

{#if eventModal}
	<EventListMessageModal event={eventModal} onclose={() => (eventModal = null)} />
{/if}

{#if replyModal}
	<ChatInputReply event={replyModal} onclose={() => (replyModal = null)} />
{/if}

{#if sendTargetModal}
	<SendTargetModal onclose={() => (sendTargetModal = false)} />
{/if}

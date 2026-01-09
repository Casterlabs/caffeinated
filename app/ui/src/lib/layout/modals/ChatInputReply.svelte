<script lang="ts">
	import type EventHandler from '$lib/event-handler';
	import { type RichMessageEvent } from '$lib/koi';

	import Modal from '../Modal.svelte';
	import { IconChatBubbleLeftEllipsis, IconXMark } from '@casterlabs/heroicons-svelte';

	interface Props {
		event: RichMessageEvent;
		uiEvents: EventHandler;
		onclose: () => void;
	}

	let { event: replyTarget, uiEvents, onclose }: Props = $props();
</script>

{#snippet actionButton(title: string, Icon: any, onclick: () => void)}
	<button class="basis-full flex-grow-1 hover:bg-base-2 active:bg-base-2 py-4 rounded-lg flex flex-col items-center justify-center space-y-2" ontouchend={onclick} {onclick}>
		<Icon theme="outline" />
		<span class="text-xs"> {title} </span>
	</button>
{/snippet}

{#snippet title()}
	<div class="flex items-center justify-center">
		Replying to {replyTarget.sender.username}
	</div>
{/snippet}

<Modal {title} {onclose}>
	<div class="flex items-center justify-stretch w-full">
		{@render actionButton('Cancel Reply', IconXMark, () => {
			uiEvents.broadcast('x-start-reply', null);
			onclose();
		})}

		{@render actionButton('Show Message', IconChatBubbleLeftEllipsis, () => {
			uiEvents.broadcast('x-find-message', replyTarget.meta_id);
			onclose();
		})}
	</div>
</Modal>

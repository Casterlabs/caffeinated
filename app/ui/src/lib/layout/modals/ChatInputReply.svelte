<script lang="ts">
	import type EventHandler from '$lib/event-handler';
	import { type RichMessageEvent } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
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
		<span class="text-xs">
			<LocalizedText key={title} />
		</span>
	</button>
{/snippet}

{#snippet title()}
	<div class="flex items-center justify-center">
		<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.reply_menu.title" args={{ name: replyTarget.sender.username }} />
	</div>
{/snippet}

<Modal {title} {onclose}>
	<div class="flex items-center justify-stretch w-full">
		{@render actionButton('co.casterlabs.caffeinated.app.docks.chat.viewer.reply_menu.cancel', IconXMark, () => {
			uiEvents.broadcast('x-start-reply', null);
			onclose();
		})}

		{@render actionButton('co.casterlabs.caffeinated.app.docks.chat.viewer.reply_menu.show', IconChatBubbleLeftEllipsis, () => {
			uiEvents.broadcast('x-find-message', replyTarget.meta_id);
			onclose();
		})}
	</div>
</Modal>

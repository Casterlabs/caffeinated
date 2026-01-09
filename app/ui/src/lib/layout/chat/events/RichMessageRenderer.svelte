<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';
	import { type MessageMetaEvent, type PlatformMessageEvent, type RichMessageEvent } from '$lib/koi';

	import UsernameRenderer from '../UsernameRenderer.svelte';
	import { IconCake, IconChatBubbleLeft, IconMegaphone } from '@casterlabs/heroicons-svelte';

	import { onMount } from 'svelte';

	interface Props {
		event: RichMessageEvent | PlatformMessageEvent;
		uiEvents: EventHandler;
	}

	let { event, uiEvents }: Props = $props();

	let isDeleted = $state(!event.is_visible);
	let upvotes = $state(event.upvotes);

	let replyTargetDeleted = $state(!event.x_reply_target_data?.is_visible || false);
	let showReplyTargetAnyways = $state(false);

	onMount(() => {
		if (event.event_type == 'PLATFORM_MESSAGE') return; // Platform messages don't have META events.

		return Koi.on('META', (newMeta: MessageMetaEvent) => {
			if (newMeta.meta_id == event.meta_id) {
				event.is_visible = newMeta.is_visible; // Mutate the object.
				event.upvotes = newMeta.upvotes;

				isDeleted = !newMeta.is_visible;
				upvotes = newMeta.upvotes;
			} else if (newMeta.meta_id == event.reply_target) {
				replyTargetDeleted = !newMeta.is_visible;

				if (event.x_reply_target_data) {
					event.x_reply_target_data.is_visible = newMeta.is_visible; // Mutate the object.
				}
			}
		});
	});
</script>

<span class="rich-message" class:text-xs={event.event_type == 'PLATFORM_MESSAGE'} class:italic={event.attributes.includes('RP_ACTION')}>
	{#if event.attributes.includes('FIRST_TIME_CHATTER')}
		<span class="text-gray-200 mt-0.5 block text-sm">
			<IconCake theme="mini" class="-translate-y-0.5" />
			First time chatter
		</span>
	{/if}

	{#if event.attributes.includes('ANNOUNCEMENT')}
		<span class="text-gray-200 mt-0.5 block text-sm">
			<IconMegaphone theme="mini" class="-translate-y-0.5" />
			Announcement
		</span>
	{/if}

	{#if event.reply_target}
		<span class="text-gray-200 mt-0.5 block text-sm">
			{#if event.x_reply_target_data}
				<!-- 
					NB `!isDeleted`:
					Always show who they're replying to if we're deleted. 
					Because that means that the user has already consented to seeing potentially awful message content.
				-->
				{#if replyTargetDeleted && !showReplyTargetAnyways && !isDeleted}
					<IconChatBubbleLeft theme="mini" class="-translate-y-0.5" />
					Replying to a deleted message
					<button class="link text-xs" onclick={() => (showReplyTargetAnyways = true)}> Show </button>
				{:else}
					<button
						class="text-ellipsis overflow-hidden whitespace-nowrap max-w-full"
						onclick={(e) => {
							e.stopPropagation();
							uiEvents.broadcast('x-find-message', event.reply_target);
						}}
					>
						<IconChatBubbleLeft theme="mini" class="-translate-y-0.5" />
						Replying to
						<UsernameRenderer user={event.x_reply_target_data.sender} showBadges={false} />
						{@html event.x_reply_target_data.html}
					</button>
					{#if showReplyTargetAnyways && !isDeleted}
						<button class="link text-xs" onclick={() => (showReplyTargetAnyways = false)}> Hide </button>
					{/if}
				{/if}
			{:else if event.reply_target}
				Replying to another message.
			{/if}
		</span>
	{/if}

	{#if event.event_type != 'PLATFORM_MESSAGE'}
		<UsernameRenderer user={event.sender} />
	{/if}

	<span
		class="message-contents"
		class:font-bold={event.attributes.includes('RP_ACTION')}
		class:upvote-1={upvotes > 0}
		class:upvote-2={upvotes > 10}
		class:upvote-3={upvotes > 100}
		class:upvote-4={upvotes > 1000}
	>
		{#if event.event_type == 'PLATFORM_MESSAGE'}
			{@html event.html.replace(/\n/g, '<br />')}
		{:else}
			{@html event.html}
		{/if}
	</span>
</span>

<style>
	.rich-message :global([data-rich-type='mention']) {
		font-weight: bolder;
	}

	.rich-message :global([data-rich-type='link']) {
		color: var(--primary11);
		text-decoration: underline;
	}

	/* Upvotes */

	.rich-message :global(.upvote-counter) {
		display: none;
		font-weight: bold;
	}

	.rich-message .upvote-1 :global(.upvote-counter) {
		/* 1+ */
		color: #ff00ff;
		display: inline-block;
	}

	.rich-message .upvote-2 :global(.upvote-counter) {
		/* 10+ */
		color: #00ff00;
		display: inline-block;
	}

	.rich-message .upvote-3 :global(.upvote-counter) {
		/* 100+ */
		color: #ffff00;
		display: inline-block;
	}

	.rich-message .upvote-4 :global(.upvote-counter) {
		/* 1000+ */
		color: #ffffff;
		display: inline-block;
	}
</style>

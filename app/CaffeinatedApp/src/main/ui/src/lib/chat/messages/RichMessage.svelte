<svelte:options accessors />

<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import ContextMenu from '$lib/ui/ContextMenu.svelte';
	import EmojiText from '$lib/EmojiText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { onMount } from 'svelte';
	import { t } from '$lib/app.mjs';
	import User from './User.svelte';

	const PLATFORMS_WITH_BAN = []; // TODO
	const PLATFORMS_WITH_TIMEOUT = []; // TODO
	const PLATFORMS_WITH_RAID = []; // TODO

	export let event;
	export let onContextMenuAction;
	export let supportedFeatures;
	let container;

	export let upvotes = event.upvotes;
	export let isDeleted = false;
	export let reactions = []; // An array of emojis.

	$: reactionsWithCount = reactions.reduce(
		(acc, value) => {
			acc[value] = (acc[value] || 0) + 1;
			return acc;
		}, //
		{}
	);

	function updateUpvotes() {
		if (!container) return;

		const counter = container.querySelector('.upvote-counter');
		if (counter) counter.innerHTML = upvotes;
	}

	$: upvotes, updateUpvotes();
	onMount(updateUpvotes);
</script>

<ContextMenu
	items={[
		{
			type: 'button',
			icon: 'icon/no-symbol',
			text: 'co.casterlabs.caffeinated.app.docks.chat.viewer.action.ban',
			hidden: !PLATFORMS_WITH_BAN.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('ban', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/clock',
			text: 'co.casterlabs.caffeinated.app.docks.chat.viewer.action.timeout',
			hidden: !PLATFORMS_WITH_TIMEOUT.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('timeout', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/users',
			text: 'co.casterlabs.caffeinated.app.docks.chat.viewer.action.raid',
			hidden: !PLATFORMS_WITH_RAID.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('raid', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/arrow-up',
			text: 'co.casterlabs.caffeinated.app.docks.chat.viewer.action.upvote',
			hidden: !supportedFeatures[event.sender.platform]?.includes('MESSAGE_UPVOTE'),
			onclick() {
				onContextMenuAction('upvote', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/trash',
			text: 'co.casterlabs.caffeinated.app.docks.chat.viewer.action.delete_message',
			color: 'error',
			hidden: isDeleted || !supportedFeatures[event.sender.platform]?.includes('MESSAGE_DELETION'),
			onclick() {
				onContextMenuAction('delete', event);
			}
		}
	]}
>
	<div
		class="transition inline-block"
		class:opacity-60={isDeleted}
		class:hover:opacity-100={isDeleted}
		bind:this={container}
	>
		{#if event.attributes.includes('FIRST_TIME_CHATTER')}
			<span class="block text-sm opacity-80 mt-0.5">
				<icon class="inline-block h-4 w-4 translate-y-1" data-icon="icon/cake" />
				<LocalizedText
					key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RICH_MESSAGE.first_time_chatter"
				/>
			</span>
		{/if}
		{#if event.attributes.includes('ANNOUNCEMENT')}
			<span class="block text-sm opacity-80 mt-0.5">
				<icon class="inline-block h-4 w-4 translate-y-1" data-icon="icon/megaphone" />
				<LocalizedText
					key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RICH_MESSAGE.announcement"
				/>
			</span>
		{/if}
		{#if event.reply_target_data || event.reply_target}
			<a
				href="#"
				class="block text-sm opacity-80 mt-0.5"
				on:click={() => {
					const ANIMATE_TIME = 750;
					event.reply_target_data.element.scrollIntoView();

					event.reply_target_data.element.style.transition = `background-color ${ANIMATE_TIME}ms`;
					event.reply_target_data.element.style.backgroundColor = 'var(--primary8)';

					setTimeout(() => {
						event.reply_target_data.element.style.backgroundColor = '';

						setTimeout(() => {
							event.reply_target_data.element.style.transition = '';
						}, ANIMATE_TIME);
					}, ANIMATE_TIME);
				}}
			>
				<icon class="inline-block h-4 w-4 translate-y-1" data-icon="icon/chat-bubble-left" />
				{#if event.reply_target_data}
					<LocalizedText
						key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RICH_MESSAGE.replying_to"
						slotMapping={['other', 'message']}
					>
						<span slot="0">
							<User user={event.reply_target_data.event.sender} /><span aria-hidden="true">
								<span class="select-none font-light"> : </span>
								<span class="opacity-0 absolute"> &gt;&nbsp; <!-- Sexy Text Selection --></span>
							</span>
						</span>
						<!-- svelte-ignore a11y-click-events-have-key-events -->
						<!-- svelte-ignore a11y-missing-attribute -->
						<!-- svelte-ignore a11y-no-static-element-interactions -->
						<!-- svelte-ignore a11y-invalid-attribute -->
						<span slot="1">
							{@html event.reply_target_data.event.html}
						</span>
					</LocalizedText>
				{:else if event.reply_target}
					<LocalizedText
						key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RICH_MESSAGE.replying_to_unknown"
					/>
				{/if}
			</a>
		{/if}
		{#if event.attributes.includes('RP_ACTION')}
			<i>
				<User user={event.sender} /><span aria-hidden="true">
					<span class="select-none font-light"> : </span>
					<span class="opacity-0 absolute"> &gt;&nbsp; <!-- Sexy Text Selection --></span>
				</span>

				<b
					class="richmessage-message"
					style:--platform-color={streamingServices[event.sender.platform]?.color}
					style:--user-color={event.sender.color}
					class:upvote-1={upvotes > 0}
					class:upvote-2={upvotes > 10}
					class:upvote-3={upvotes > 100}
					class:upvote-4={upvotes > 1000}
				>
					{@html event.html}
				</b>
			</i>
		{:else}
			<User user={event.sender} /><span aria-hidden="true">
				<span class="select-none font-light"> : </span>
				<span class="opacity-0 absolute"> &gt;&nbsp; <!-- Sexy Text Selection --></span>
			</span>

			<span
				class="richmessage-message"
				class:upvote-1={upvotes > 0}
				class:upvote-2={upvotes > 10}
				class:upvote-3={upvotes > 100}
				class:upvote-4={upvotes > 1000}
			>
				{@html event.html}
			</span>
		{/if}

		{#if reactions.length > 0}
			<div class="w-full text-xs -mt-1 pt-1.5">
				{#each Object.entries(reactionsWithCount) as [reaction, count]}
					<span class="bg-base-6 rounded px-1 py-0.5">
						<span class="inline-block -translate-y-px">
							<EmojiText sequence={reaction} />
							<small class="-ml-0.5 mr-px">
								{count}
							</small>
						</span>
					</span>
				{/each}
			</div>
		{/if}
	</div>
</ContextMenu>

<style>
	:global(.richmessage-message [data-rich-type='mention']) {
		font-weight: 600;
	}

	:global(.richmessage-message [data-rich-type='link']) {
		text-decoration: underline;
		color: var(--link);
	}

	/* Upvotes */
	:global(.upvote-counter) {
		display: none;
		font-weight: bold;
	}

	.upvote-1 :global(.upvote-counter) {
		/* 1+ */
		color: #ff00ff;
		display: inline-block;
	}

	.upvote-2 :global(.upvote-counter) {
		/* 10+ */
		color: #00ff00;
		display: inline-block;
	}

	.upvote-3 :global(.upvote-counter) {
		/* 100+ */
		color: #ffff00;
		display: inline-block;
	}

	.upvote-4 :global(.upvote-counter) {
		/* 1000+ */
		color: #ffffff;
		display: inline-block;
	}
</style>

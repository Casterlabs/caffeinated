<svelte:options accessors />

<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import ContextMenu from '$lib/ui/ContextMenu.svelte';
	import EmojiText from '$lib/EmojiText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { onMount } from 'svelte';
	import { t } from '$lib/app.mjs';
	import User from './User.svelte';

	const PLATFORMS_WITH_BAN = ['TWITCH', 'TROVO', 'BRIME'];
	const PLATFORMS_WITH_TIMEOUT = ['TWITCH', 'TROVO', 'BRIME'];
	const PLATFORMS_WITH_RAID = ['CAFFEINE', 'TWITCH', 'TROVO'];

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

	// Either if `is_highlighted` or `donations` > 0.
	$: highlighted = event?.is_highlighted || event?.donations.length > 0 || false;

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
		class:highlight={highlighted}
		class:opacity-60={isDeleted}
		class:hover:opacity-100={isDeleted}
		bind:this={container}
	>
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

	/* Highlight */
	:global(li.message-container):has(.highlight) {
		border-radius: 0.375rem; /* rounded-md */
		padding: 0.5rem; /* p-2 */
		background-color: var(--primary5); /* bg-primary-5 */
		text-shadow: 1px 1px 5px var(--primary3); /* Try to make the text more readable */
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

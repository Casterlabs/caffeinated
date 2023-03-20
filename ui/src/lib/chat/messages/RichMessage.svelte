<svelte:options accessors />

<script>
	import { onMount } from 'svelte';
	import ContextMenu from '../../ui/ContextMenu.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	const PLATFORMS_WITH_BAN = ['TWITCH', 'TROVO', 'BRIME'];
	const PLATFORMS_WITH_TIMEOUT = ['TWITCH', 'TROVO', 'BRIME'];
	const PLATFORMS_WITH_RAID = ['CAFFEINE', 'TWITCH', 'TROVO'];
	const PLATFORMS_WITH_DELETE = ['TWITCH', 'BRIME', 'TROVO', 'DLIVE' /*"YOUTUBE"*/];
	const PLATFORMS_WITH_UPVOTE = ['CAFFEINE'];

	export let event;
	export let onContextMenuAction;
	let container;

	export let upvotes = event.upvotes;
	export let isDeleted = false;

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
			text: 'chat.viewer.action.ban',
			hidden: !PLATFORMS_WITH_BAN.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('ban', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/clock',
			text: 'chat.viewer.action.timeout',
			hidden: !PLATFORMS_WITH_TIMEOUT.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('timeout', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/users',
			text: 'chat.viewer.action.raid',
			hidden: !PLATFORMS_WITH_RAID.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('raid', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/arrow-up',
			text: 'chat.viewer.action.upvote',
			hidden: !PLATFORMS_WITH_UPVOTE.includes(event.sender.platform),
			onclick() {
				onContextMenuAction('upvote', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/trash',
			text: 'chat.viewer.action.delete_message',
			color: 'error',
			hidden: isDeleted || !PLATFORMS_WITH_DELETE.includes(event.sender.platform),
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
		<span class="richmessage-badges space-x-1" aria-hidden="true">
			{#each event.sender.badges as badge}
				<img class="inline-block h-4 -translate-y-0.5" alt="" src={badge} />
			{/each}
		</span>

		<b
			>{event.sender.displayname}<span class="user-platform">
				<LocalizedText key="platform.parenthesis.{event.streamer.platform}" /></span
			></b
		><span aria-hidden="true">
			<span class="select-none"> : </span>
			<span class="opacity-0 absolute"> &gt;&nbsp; <!-- Sexy Text Selection --></span>
		</span>

		<span
			class:upvote-1={upvotes > 0}
			class:upvote-2={upvotes > 10}
			class:upvote-3={upvotes > 100}
			class:upvote-4={upvotes > 1000}
		>
			{@html event.html}
		</span>

		<!-- TODO Donations -->
	</div>
</ContextMenu>

<style>
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

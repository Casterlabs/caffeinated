<svelte:options accessors />

<script>
	import ContextMenu from '../../ui/ContextMenu.svelte';

	const PLATFORMS_WITH_BAN = ['TWITCH', 'TROVO', 'BRIME'];
	const PLATFORMS_WITH_TIMEOUT = ['TWITCH', 'TROVO', 'BRIME'];
	const PLATFORMS_WITH_RAID = ['CAFFEINE', 'TWITCH', 'TROVO'];
	const PLATFORMS_WITH_DELETE = ['TWITCH', 'BRIME', 'TROVO', 'DLIVE' /*"YOUTUBE"*/];
	const PLATFORMS_WITH_UPVOTE = ['CAFFEINE'];

	export let event;
	export let onContextMenuAction;

	export let upvotes = event.upvotes;
	export let isDeleted = false;

	// Either if `is_highlighted` or `donations` > 0.
	$: highlighted = event?.is_highlighted || event?.donations.length > 0 || false;
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
	>
		<span class="richmessage-badges space-x-1" aria-hidden="true">
			{#each event.sender.badges as badge}
				<img class="inline-block h-4 -translate-y-0.5" alt="" src={badge} />
			{/each}
		</span>

		<span style:color={event.sender.color || 'red'}> {event.sender.displayname}</span><span
			aria-hidden="true"
		>
			<span class="select-none"> : </span>
			<span class="opacity-0 absolute"> &gt;&nbsp; <!-- Sexy Text Selection --></span>
		</span>

		<span>
			{@html event.html}
		</span>{#if upvotes > 0}
			<sup class="upvote-counter">
				{#if upvotes < 10}
					<span class="upvote-1">{upvotes}</span>
				{:else if upvotes < 100}
					<span class="upvote-2">{upvotes}</span>
				{:else if upvotes < 1000}
					<span class="upvote-3">{upvotes}</span>
				{:else}
					<span class="upvote-4">{upvotes}</span>
				{/if}
			</sup>
		{/if}

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
	.upvote-1 {
		/* 1+ */
		color: #ff00ff;
	}

	.upvote-2 {
		/* 10+ */
		color: #00ff00;
	}

	.upvote-3 {
		/* 100+ */
		color: #ffff00;
	}

	.upvote-4 {
		/* 1000+ */
		color: #ffffff;
	}
</style>

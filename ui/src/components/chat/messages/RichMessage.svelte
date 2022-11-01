<svelte:options accessors />

<script>
	import ContextMenu from '../../interaction/ContextMenu.svelte';

	export let event;
	export let onContextMenuAction;

	export let upvotes = event.upvotes;
	export let isDeleted = false;
</script>

<ContextMenu
	items={[
		{
			type: 'button',
			icon: 'icon/no-symbol',
			text: 'chat.viewer.action.ban',
			onclick() {
				onContextMenuAction('ban', event);
			}
		},
		{
			type: 'button',
			icon: 'icon/clock',
			text: 'chat.viewer.action.timeout',
			onclick() {
				onContextMenuAction('timeout', event);
			}
		},
		{ type: 'divider', hidden: isDeleted },
		{
			type: 'button',
			icon: 'icon/trash',
			text: 'chat.viewer.action.delete_message',
			color: 'error',
			hidden: isDeleted,
			onclick() {
				onContextMenuAction('delete', event);
			}
		}
	]}
>
	<div class="transition" class:opacity-60={isDeleted} class:hover:opacity-100={isDeleted}>
		<span class="richmessage-badges space-x-1" aria-hidden="true">
			{#each event.sender.badges as badge}
				<img class="inline-block h-4 -translate-y-0.5" alt="" src={badge} />
			{/each}
		</span>

		<span style:color={event.sender.color || 'red'}>{event.sender.displayname}</span>:

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

<svelte:options accessors />

<script>
	import { onMount } from 'svelte';

	let container;

	export let event;
	export let settings; // svelte store

	$: badges = $settings['message_style.badges'];

	export let upvotes = event.upvotes;

	function updateUpvotes() {
		if (!container) return;

		const counter = container.querySelector('.upvote-counter');
		if (counter) counter.innerHTML = upvotes;
	}
	$: upvotes, updateUpvotes();
	onMount(updateUpvotes);
</script>

<span class="inline-block" bind:this={container}>
	{#if badges == 'Before Username'}
		<span class="richmessage-badges space-x-1" aria-hidden="true">
			{#each event.sender.badges as badge}
				<img class="inline-block align-middle h-[1em] -translate-y-0.5" alt="" src={badge} />
			{/each}
		</span>
	{/if}

	<b style:--user-color={event.sender.color || 'red'}>
		<icon
			class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
			data-icon="service/{event.streamer.platform.toLowerCase()}"
			alt=""
		/>
		{event.sender.displayname}</b
	><span>:</span>

	{#if badges == 'After Username'}
		<span class="richmessage-badges space-x-1" aria-hidden="true">
			{#each event.sender.badges as badge}
				<img class="inline-block align-middle h-[1em] -translate-y-0.5" alt="" src={badge} />
			{/each}
		</span>
	{/if}

	<span
		class:upvote-1={upvotes > 0}
		class:upvote-2={upvotes > 10}
		class:upvote-3={upvotes > 100}
		class:upvote-4={upvotes > 1000}
	>
		{#if event.reply_target_data}
			<!-- Add an @ to the user. TODO use the lang system. -->
			<b>@{event.reply_target_data.event.sender.username}</b>
		{/if}
		{@html event.html}
	</span>
</span>

<style>
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

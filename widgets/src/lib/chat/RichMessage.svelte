<svelte:options accessors />

<script>
	import { onMount } from 'svelte';
	import { fade } from 'svelte/transition';

	let container;

	export let event;
	export let settings; // svelte store

	export let upvotes = event.upvotes;
	export let visible = false;

	function updateUpvotes() {
		if (!container) return;

		const counter = container.querySelector('.upvote-counter');
		if (counter) counter.innerHTML = upvotes;
	}
	$: upvotes, updateUpvotes();
	onMount(updateUpvotes);

	let disappearTask = -1;
	function checkDisappear() {
		clearTimeout(disappearTask);

		if (!$settings['message_style.disappearing']) {
			visible = true;
			return;
		}

		const secondsSinceAdded = Date.now() - new Date(event.timestamp);
		const disappearAfter = $settings['message_style.disappear_after'] * 1000;
		const disappearIn = disappearAfter - secondsSinceAdded;

		if (disappearIn > 0) {
			visible = true;
			disappearTask = setTimeout(() => {
				visible = false;
			}, disappearIn);
		} else {
			visible = false;
		}
	}
	onMount(() => settings.subscribe(checkDisappear));
</script>

{#if visible}
	{@const badges = $settings['message_style.badges']}

	<span
		class="inline-block"
		bind:this={container}
		in:fade={{ duration: 200 }}
		out:fade={{ duration: 750 }}
	>
		{#if badges == 'Before Username'}
			<span class="richmessage-badges space-x-1" aria-hidden="true">
				{#each event.sender.badges as badge}
					<img class="inline-block h-4 -translate-y-0.5" alt="" src={badge} />
				{/each}
			</span>
		{/if}

		<b style:color={event.sender.color || 'red'}>{event.sender.displayname}</b><span>:</span>

		{#if badges == 'After Username'}
			<span class="richmessage-badges space-x-1" aria-hidden="true">
				{#each event.sender.badges as badge}
					<img class="inline-block h-4 -translate-y-0.5" alt="" src={badge} />
				{/each}
			</span>
		{/if}

		<span
			class:upvote-1={upvotes > 0}
			class:upvote-2={upvotes > 10}
			class:upvote-3={upvotes > 100}
			class:upvote-4={upvotes > 1000}
		>
			{@html event.html}
		</span>
	</span>
{/if}

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

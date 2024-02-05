<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import User from './User.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.CHANNEL_POINTS"
	slotMapping={['name', 'reward']}
>
	<User slot="0" user={event.sender} />
	<span slot="1">
		<img
			alt=""
			class="channel-points-reward-image"
			src={event.reward.reward_image || event.reward.default_reward_image}
		/>
		{event.reward.title}
	</span>
</LocalizedText>

{#if event.prompt}
	{event.prompt}:
	{#if event.message}
		{event.message}
	{:else}
		<i class="opacity-90">No message provided</i>
	{/if}
{/if}

<style>
	.channel-points-reward-image {
		display: inline-block;
		height: 0.8em;
	}
</style>

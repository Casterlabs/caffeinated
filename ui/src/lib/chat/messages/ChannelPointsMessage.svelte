<script>
	import LocalizedText from '../../LocalizedText.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="chat.viewer.message.CHANNELPOINTS"
	opts={{
		displayname: event.follower.displayname,
		reward: event.reward.title
	}}
	slotMapping={['image']}
>
	<img
		slot="0"
		alt=""
		class="channel-points-reward-image"
		src={event.reward.reward_image || event.reward.default_reward_image}
	/>
</LocalizedText>

<style>
	.channel-points-reward-image {
		height: 0.8em;
	}
</style>

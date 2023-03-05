<script>
	export let event;
</script>

<b>{event.sender.displayname}</b>
just redeemed
<img
	alt=""
	class="channel-points-reward-image"
	src={event.reward.reward_image || event.reward.default_reward_image}
/>
<b>{event.reward.title}</b>

<style>
	.channel-points-reward-image {
		height: 0.8em;
	}
</style>

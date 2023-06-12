<script>
	export let event;
</script>

<b style:--user-color={event.sender.color || 'red'}>
	<icon
		class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
		data-icon="service/{event.streamer.platform.toLowerCase()}"
		alt=""
	/>
	{event.sender.displayname}
</b>
just redeemed
<img
	alt=""
	class="channel-points-reward-image"
	src={event.reward.reward_image || event.reward.default_reward_image}
/>
<b style:--user-color={event.sender.color || 'red'}>{event.reward.title}</b>

<style>
	.channel-points-reward-image {
		display: inline-block;
		height: 0.8em;
	}
</style>

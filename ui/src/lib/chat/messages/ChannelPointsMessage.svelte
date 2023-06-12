<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { t } from '$lib/translate.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<span style:--platform-color={streamingServices[event.streamer.platform]?.color}>
	<LocalizedText
		key="chat.viewer.message.CHANNELPOINTS"
		opts={{
			displayname: event.sender.displayname,
			reward: event.reward.title,
			platform: event.streamer.platform.toLowerCase()
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
</span>

<style>
	.channel-points-reward-image {
		display: inline-block;
		height: 0.8em;
	}
</style>

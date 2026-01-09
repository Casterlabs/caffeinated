<script lang="ts">
	import type { ChannelPointsEvent } from '$lib/koi';

	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: ChannelPointsEvent;
	}

	let { event }: Props = $props();
</script>

<UsernameRenderer user={event.sender} />
just redeemed
<img alt="" class="h-4" src={event.reward.reward_image || event.reward.default_reward_image} />
{event.reward.title}!

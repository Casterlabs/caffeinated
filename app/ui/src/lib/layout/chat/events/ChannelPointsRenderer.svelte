<script lang="ts">
	import type { ChannelPointsEvent } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: ChannelPointsEvent;
	}

	let { event }: Props = $props();
</script>

{#snippet name()}
	<UsernameRenderer user={event.sender} />
{/snippet}

{#snippet reward()}
	<img alt="" class="h-4 inline-block" src={event.reward.reward_image || event.reward.default_reward_image} />
	{event.reward.title}
{/snippet}

<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.CHANNEL_POINTS" components={{ name, reward }} />

{#if event.reward.prompt}
	<br />
	<strong>{event.reward.prompt}</strong>:
	{#if event.reward.user_input}
		{event.reward.user_input}
	{:else}
		<i class="opacity-90">
			<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.CHANNEL_POINTS.prompt.no_input" />
		</i>
	{/if}
{/if}

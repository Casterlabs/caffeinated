<script lang="ts">
	import type { RaidEvent } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: RaidEvent;
	}

	let { event }: Props = $props();
</script>

{#snippet name()}
	<UsernameRenderer user={event.host} />
{/snippet}

<LocalizedText
	key={event.viewers > 1
		? 'co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID.viewers'
		: 'co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID.no_viewers'}
	components={{ name }}
	args={{ viewers: event.viewers }}
/>

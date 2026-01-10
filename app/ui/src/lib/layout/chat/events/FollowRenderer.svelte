<script lang="ts">
	import type { FollowEvent } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: FollowEvent;
	}

	let { event }: Props = $props();
</script>

{#snippet name()}
	<UsernameRenderer user={event.follower} />
{/snippet}

<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.FOLLOW" components={{ name }} />

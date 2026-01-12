<script lang="ts">
	import type { ViewerJoinLeaveEvent } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: ViewerJoinLeaveEvent;
	}

	let { event }: Props = $props();
</script>

{#snippet name()}
	<UsernameRenderer user={event.viewer} />
{/snippet}

<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.VIEWER_JOIN" components={{ name }} />

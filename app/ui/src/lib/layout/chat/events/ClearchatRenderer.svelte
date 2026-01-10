<script lang="ts">
	import type { ClearChatEvent } from '$lib/koi';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';

	interface Props {
		event: ClearChatEvent;
	}

	let { event }: Props = $props();
</script>

<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.CLEAR_CHAT" />

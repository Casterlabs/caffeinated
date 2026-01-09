<script lang="ts">
	import type { ClearChatEvent } from '$lib/koi';

	interface Props {
		event: ClearChatEvent;
	}

	let { event }: Props = $props();
</script>

Chat was cleared.

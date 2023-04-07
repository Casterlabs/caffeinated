<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import { t } from '$lib/translate.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="chat.viewer.message.VIEWER_LEAVE"
	opts={{
		displayname: event.viewer.displayname,
		platform: event.streamer.platform.toLowerCase()
	}}
/>

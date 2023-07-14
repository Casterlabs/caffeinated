<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { t } from '$lib/translate.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<span style:--platform-color={streamingServices[event.streamer.platform]?.color}>
	<LocalizedText
		key="chat.viewer.message.VIEWER_LEAVE"
		opts={{
			displayname: event.viewer.displayname,
			platform: event.streamer.platform.toLowerCase()
		}}
	/>
</span>

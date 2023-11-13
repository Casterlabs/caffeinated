<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { t } from '$lib/app.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<span
	style:--platform-color={streamingServices[event.streamer.platform]?.color}
	style:--user-color={event.host.color}
>
	<LocalizedText
		key="chat.viewer.message.RAID"
		opts={{
			displayname: event.host.displayname,
			viewers: event.viewers,
			platform: event.streamer.platform.toLowerCase()
		}}
	/>
</span>

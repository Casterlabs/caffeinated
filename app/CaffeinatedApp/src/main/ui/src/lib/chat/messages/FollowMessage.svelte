<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { t } from '$lib/app.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<span
	style:--platform-color={streamingServices[event.streamer.platform]?.color}
	style:--user-color={event.follower.color}
>
	<LocalizedText
		key="chat.viewer.message.FOLLOW"
		opts={{
			displayname: event.follower.displayname,
			platform: event.streamer.platform.toLowerCase()
		}}
	/>
</span>

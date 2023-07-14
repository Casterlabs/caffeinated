<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import streamingServices from '$lib/streamingServices.mjs';
	import { t } from '$lib/translate.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<span style:--platform-color={streamingServices[event.streamer.platform]?.color}>
	<LocalizedText
		key="chat.viewer.message.SUBSCRIPTION.{event.sub_type}"
		opts={{
			months: event.months,
			displayname: event.subscriber?.displayname,
			gifter: event.subscriber?.displayname,
			recipient: event.gift_recipient?.displayname,
			platform: event.streamer.platform.toLowerCase()
		}}
	/>
</span>

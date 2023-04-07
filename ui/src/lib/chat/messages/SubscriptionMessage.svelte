<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import { t } from '$lib/translate.mjs';

	export let event;
	export let onContextMenuAction;
</script>

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

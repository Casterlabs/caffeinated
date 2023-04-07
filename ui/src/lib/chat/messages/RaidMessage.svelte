<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import { t } from '$lib/translate.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="chat.viewer.message.RAID"
	opts={{
		displayname: event.host.displayname,
		viewers: event.viewers,
		platform: event.streamer.platform.toLowerCase()
	}}
/>

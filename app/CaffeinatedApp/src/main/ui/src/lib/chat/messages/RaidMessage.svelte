<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import User from './User.svelte';

	import streamingServices from '$lib/streamingServices.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID"
	opts={{
		viewers: event.viewers
	}}
	slotMapping={['name', 'viewers']}
>
	<User slot="0" user={event.host} />
	<b
		slot="1"
		style:--platform-color={streamingServices[event.streamer.platform]?.color}
		style:--user-color={event.host.color}
	>
		{event.viewers}
	</b>
</LocalizedText>

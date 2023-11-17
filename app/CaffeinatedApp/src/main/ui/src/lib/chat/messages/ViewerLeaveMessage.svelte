<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import User from './User.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.VIEWER_LEAVE"
	slotMapping={['name']}
>
	<User slot="0" user={event.viewer} />
</LocalizedText>

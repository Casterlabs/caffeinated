<script>
	import LocalizedText from '../../LocalizedText.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="chat.viewer.message.VIEWER_LEAVE"
	opts={{ displayname: event.viewer.displayname }}
/>

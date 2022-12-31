<script>
	import LocalizedText from '../../LocalizedText.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="chat.viewer.message.VIEWER_JOIN"
	opts={{ displayname: event.viewer.displayname }}
/>

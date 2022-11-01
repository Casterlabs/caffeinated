<script>
	import LocalizedText from '../../LocalizedText.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="chat.viewer.message.RAID"
	opts={{ displayname: event.host.displayname, viewers: event.viewers }}
/>

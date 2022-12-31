<script>
	import LocalizedText from '../../LocalizedText.svelte';

	export let event;
	export let onContextMenuAction;
</script>

<span class="viewer-joinleave">
	<LocalizedText
		key="chat.viewer.message.VIEWER_LEAVE"
		opts={{ displayname: event.viewer.displayname }}
	/>
</span>

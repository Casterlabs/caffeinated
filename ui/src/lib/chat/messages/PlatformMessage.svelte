<script>
	import streamingServices from '$lib/streamingServices.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<small
	class="platform-message"
	style:--platform-color={streamingServices[event.streamer.platform]?.color}
>
	<icon
		class="inline-block h-5 -translate-y-0.5"
		data-icon="service/{event.sender.username.toLowerCase()}"
	/>
	&gt;
	<i>{@html event.html}</i>
</small>

<style>
	.platform-message :global(a) {
		color: var(--link) !important;
	}
</style>

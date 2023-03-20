<script>
	export let event;
	export let onContextMenuAction;
</script>

<small class="platform-message">
	<img
		class="inline-block h-5 -translate-y-0.5"
		alt={event.sender.displayname}
		src="/images/services/{event.sender.username}/icon.svg"
	/>
	&gt;
	<i>{@html event.html}</i>
</small>

<style>
	.platform-message :global(a) {
		color: var(--link) !important;
	}
</style>

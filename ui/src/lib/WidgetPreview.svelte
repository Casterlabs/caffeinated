<script>
	export let ariaHidden = true;
	export let widget;
	export let mode; // Set to any value to override.

	$: url = widget //
		? mode
			? widget.url.replace(/&mode=\w+/, `&mode=${mode}`) // Override the mode.
			: widget.url // No override mode, use regular URL.
		: null; // No widget, hide the frame.
</script>

{#if url}
	<iframe class="w-full h-full" aria-hidden={ariaHidden} title="" src={url} />
{/if}

<script>
	import LocalizedText from './LocalizedText.svelte';
	import AspectVar from './aspect-ratio/AspectVar.svelte';

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
	{#if mode == 'DOCK'}
		<iframe class="w-full h-full" aria-hidden={ariaHidden} title="" src={url} />
	{:else}
		<div class="max-w-md mx-auto mt-6 mb-8">
			<h1 class="font-semibold text-xl">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.preview" />
			</h1>

			<div class="w-full mt-1 select-none border border-base-8 shadow-xl rounded-md">
				<AspectVar aspectRatio={widget.details.demoAspectRatio}>
					<iframe class="w-full h-full" aria-hidden={ariaHidden} title="" src={url} />
				</AspectVar>
			</div>
		</div>
	{/if}
{/if}

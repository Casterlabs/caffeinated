<script>
	import createConsole from '$lib/console-helper.mjs';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount } from 'svelte';

	const console = createConsole('Applet');

	let widget;

	$: url = widget //
		? widget.url.replace(/&mode=\w+/, `&mode=SETTINGS_APPLET`) // Override the mode.
		: null; // No widget, hide the frame.

	onMount(() => {
		const id = $page.url.searchParams.get('id');

		Caffeinated.pluginIntegration.widgets
			.then((widgets) => {
				// Filter for a widget object with a matching id.
				// This'll return `undefined` if there's no matching result.
				return widgets.filter((w) => w.id == id)[0];
			})
			.then((w) => {
				// If the widget is `undefined`, go back.
				if (!w) {
					goto('/$caffeinated-sdk-root$/dashboard');
					return;
				}

				// All is good.
				widget = w;
				console.debug('Widget data:', widget);
			});
	});
</script>

{#if url}
	<iframe class="w-full h-full" title="" src={url} />
{/if}

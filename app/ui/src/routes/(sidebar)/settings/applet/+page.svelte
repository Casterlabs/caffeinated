<script lang="ts">
	import { goto } from '$app/navigation';

	import WidgetPreview from '$lib/layout/WidgetPreview.svelte';

	import { onMount } from 'svelte';

	let widget: null | Awaited<typeof AppPlugins.widgets>[0] = $state(null);

	onMount(async () => {
		const id = new URLSearchParams(location.search).get('id');

		widget = await AppPlugins.widgets.then((widgets) => {
			// Filter for a widget object with a matching id.
			// This'll return `undefined` if there's no matching result.
			return widgets.filter((w) => w.id == id)[0];
		});

		// If the widget is `undefined`, go back.
		if (!widget) {
			goto('/$caffeinated-sdk-root$/dashboard');
			return;
		}
	});
</script>

<div class="flex flex-col -mx-6 -my-4" style:height="calc(100% + calc(var(--spacing) * 8))">
	{#if widget}
		<WidgetPreview {widget} mode="APPLET" ariaHidden={false} />
	{/if}
</div>

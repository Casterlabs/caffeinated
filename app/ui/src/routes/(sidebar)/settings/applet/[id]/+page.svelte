<script lang="ts">
	import { goto } from '$app/navigation';
	import type { PageData } from './$types';

	import WidgetPreview from '$lib/layout/WidgetPreview.svelte';

	import { onMount } from 'svelte';

	let { data }: { data: PageData } = $props();

	let widget: null | Awaited<typeof AppPlugins.widgets>[0] = $state(null);

	onMount(async () => {
		widget = await AppPlugins.widgets.then((widgets) => {
			// Filter for a widget object with a matching id.
			// This'll return `undefined` if there's no matching result.
			return widgets.filter((w) => w.id == data.widgetId)[0];
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

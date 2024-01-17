<script>
	import WidgetPreview from '$lib/WidgetPreview.svelte';
	import ResizableGrid from '$lib/layout/ResizableGrid.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';

	import { onMount } from 'svelte';
	import { fade } from 'svelte/transition';

	export let components;
	export let componentChoices;

	/** @type {ResizableGrid} */
	export let grid;
	export let location;
	export let onPieceUpdate; // fn
	export let current;

	$: resizingLocked = grid?.isResizingLocked;

	let component;
	let componentProps = {};

	function save({ detail: newValue }) {
		current = newValue;
		doMount();
		onPieceUpdate(location, current);
	}

	async function doMount() {
		if (!current) {
			component = null;
			return;
		}

		component = components[current];
		componentProps = { grid };

		if (!component) {
			let widget;
			for (const w of await Caffeinated.pluginIntegration.widgets) {
				if (w.id == current) {
					widget = w;
					break;
				}
			}

			component = WidgetPreview;
			componentProps = { widget, mode: 'DOCK', ariaHidden: false };
		}
	}

	onMount(doMount);
</script>

<div class="flex-1 h-full w-full relative">
	<svelte:component this={component} {...componentProps} />

	{#if !$resizingLocked}
		<div class="absolute inset-x-1 top-0 h-fit opacity-90" transition:fade={{ duration: 100 }}>
			<SlimSelectMenu
				width="full"
				options={componentChoices}
				value={current || null}
				on:value={save}
			/>
		</div>
	{/if}
</div>

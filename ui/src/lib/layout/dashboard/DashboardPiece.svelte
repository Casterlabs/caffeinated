<script>
	import WidgetPreview from '$lib/WidgetPreview.svelte';
	import WelcomeWagon from '$lib/layout/dashboard/WelcomeWagon.svelte';
	import ResizableGrid from '../ResizableGrid.svelte';
	import { onMount } from 'svelte';

	const DEFAULT_COMPONENTS = {
		welcomewagon: WelcomeWagon
	};

	/** @type {ResizableGrid} */
	export let grid;
	export let location;
	export let onPieceUpdate; // fn
	export let current;

	let component;
	let componentProps = {};

	function save() {
		onPieceUpdate(location, current);
	}

	async function doMount() {
		if (!current) {
			component = null;
			return;
		}

		component = DEFAULT_COMPONENTS[current];
		componentProps = {};

		if (!component) {
			let widget;
			for (const w of await Caffeinated.plugins.widgets) {
				if (w.id == current) {
					widget = w;
					break;
				}
			}

			component = WidgetPreview;
			componentProps = { widget, mode: 'APPLET', ariaHidden: false };
		}
	}

	onMount(doMount);
</script>

<div class="h-full w-full relative">
	<svelte:component this={component} {...componentProps} />
</div>

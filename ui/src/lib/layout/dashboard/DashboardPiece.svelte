<script>
	import WidgetPreview from '$lib/WidgetPreview.svelte';
	import WelcomeWagon from '$lib/layout/dashboard/WelcomeWagon.svelte';
	import ResizableGrid from '../ResizableGrid.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';

	import { onMount } from 'svelte';
	import { fade } from 'svelte/transition';

	const DEFAULT_COMPONENTS = {
		[null]: '',
		welcomewagon: WelcomeWagon
	};

	const componentChoices = {
		[null]: 'dashboard.customize.options.none',
		welcomewagon: 'WelcomeWagon'
	};

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

		component = DEFAULT_COMPONENTS[current];
		componentProps = { grid };

		if (!component) {
			let widget;
			for (const w of await Caffeinated.plugins.widgets) {
				if (w.id == current) {
					widget = w;
					break;
				}
			}

			component = WidgetPreview;
			componentProps = { widget, mode: 'DOCK', ariaHidden: false };
		}
	}

	onMount(() => {
		// We populate componentChoices asynchronously.
		Caffeinated.plugins.widgets.then((widgets) => {
			for (const widget of widgets) {
				if (widget.details.type == 'DOCK') {
					componentChoices[widget.id] = widget.details.friendlyName;
				}
			}
		});

		doMount();
	});
</script>

<div class="flex-1 h-full w-full relative">
	<svelte:component this={component} {...componentProps} />

	{#if !$resizingLocked}
		<div class="absolute inset-x-0 top-0 h-fit opacity-90" transition:fade={{ duration: 100 }}>
			<SlimSelectMenu
				width="full"
				options={componentChoices}
				value={current || null}
				on:value={save}
			/>
		</div>
	{/if}
</div>

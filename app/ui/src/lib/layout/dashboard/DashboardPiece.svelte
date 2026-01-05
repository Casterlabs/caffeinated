<script lang="ts">
	// import WidgetPreview from '$lib/WidgetPreview.svelte';
	import { fade } from 'svelte/transition';

	import ResizableGrid from '$lib/layout/dashboard/ResizableGrid.svelte';
	import { Select } from '@casterlabs/ui';

	import { type Component, onMount } from 'svelte';

	interface Props {
		components: Record<string, Component<any, any, any>>;
		componentChoices: Record<string, string>;
		grid: ResizableGrid;
		location: string;
		onPieceUpdate: (location: string, value: string) => void;
		current: string;
	}

	let { components, componentChoices, grid, location, onPieceUpdate, current }: Props = $props();

	let isResizingLocked = grid.isResizingLocked;

	let component: Component<any, any, any> | null = $state(null);
	let componentProps = $state({});

	async function doMount() {
		if (!current) {
			component = null;
			return;
		}

		component = components[current];
		componentProps = { grid };

		if (!component) {
			let widget;
			for (const w of await AppPlugins.widgets) {
				if (w.id == current) {
					widget = w;
					break;
				}
			}

			// component = WidgetPreview;
			// componentProps = { widget, mode: 'DOCK', ariaHidden: false };
		}
	}

	onMount(doMount);
</script>

{#if component}
	{@const TypedComponent = component as Component<any, any, any>}

	<div class="flex-1 h-full w-full relative">
		<TypedComponent {...componentProps || {}} />

		{#if !$isResizingLocked}
			<div class="absolute inset-x-1 top-1 h-fit opacity-90" transition:fade={{ duration: 100 }}>
				<Select
					class="w-full"
					value={current || null}
					onchange={(e) => {
						const current = (e.target as HTMLSelectElement).value;
						doMount();
						onPieceUpdate(location, current);
					}}
				>
					{#each Object.entries(componentChoices) as [key, name]}
						<option value={key}>{name}</option>
					{/each}
				</Select>
			</div>
		{/if}
	</div>
{/if}

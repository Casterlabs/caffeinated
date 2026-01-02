<script lang="ts">
	import { goto } from '$app/navigation';
	import { storify } from '$lib/bridgeHelper';

	import { ICONS, IconEllipsisVertical } from '@casterlabs/heroicons-svelte';
	import { Button } from '@casterlabs/ui';

	const allWidgets = storify(AppPlugins, 'widgets').readable<Awaited<typeof AppPlugins.widgets>>();
	let widgets = $derived(($allWidgets || []).filter((w) => w.details.type == 'WIDGET'));
</script>

<div class="grid gap-4 grid-cols-2 lg:grid-cols-3 2xl:grid-cols-5">
	{#each widgets as widget}
		{@const WidgetIcon = (ICONS as any)[widget.details.icon] as (typeof ICONS)['academic-cap']}

		<Button onclick={() => goto(`/$caffeinated-sdk-root$/widgets/${widget.id}`)}>
			<div class="p-3 flex items-center justify-start space-x-4">
				<WidgetIcon class="w-8 h-8" theme="solid" />

				<span class="w-full whitespace-nowrap text-ellipsis overflow-hidden text-sm font-medium text-left">{widget.name}</span>

				<Button
					borderless
					onclick={() => {
						window.event!.stopPropagation();
						alert('Widget options coming soon!');
					}}
				>
					<IconEllipsisVertical theme="mini" />
					<span class="sr-only">Widget Options</span>
				</Button>
			</div>
		</Button>
	{/each}
</div>

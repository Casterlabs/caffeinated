<script lang="ts">
	import { storify } from '$lib/bridge-helper';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { ICONS, IconDocumentDuplicate } from '@casterlabs/heroicons-svelte';
	import { Button } from '@casterlabs/ui';

	const allWidgets = storify(AppPlugins, 'widgets').readable<Awaited<typeof AppPlugins.widgets>>();
	let docks = $derived(($allWidgets || []).filter((w) => w.details.type == 'DOCK'));
</script>

<div class="grid gap-4 grid-cols-2 lg:grid-cols-3 2xl:grid-cols-5">
	{#each docks as dock}
		{@const WidgetIcon = (ICONS as any)[dock.details.icon] as (typeof ICONS)['academic-cap']}

		<Button onclick={() => AppPlugins.copyWidgetUrl(dock.id)}>
			<div class="p-3 flex items-center justify-start space-x-4">
				<WidgetIcon class="w-8 h-8" theme="solid" />

				<span class="w-full whitespace-nowrap text-ellipsis overflow-hidden text-sm font-medium text-left">
					<LocalizedText key={dock.details.friendlyName} />
				</span>

				<Button borderless>
					<IconDocumentDuplicate theme="mini" />
					<span class="sr-only">
						<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.copy_link" />
					</span>
				</Button>
			</div>
		</Button>
	{/each}
</div>

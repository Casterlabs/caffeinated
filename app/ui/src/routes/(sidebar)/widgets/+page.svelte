<script lang="ts">
	import { goto } from '$app/navigation';
	import { storify } from '$lib/bridge-helper';
	import { render } from '$lib/locale/locale';

	import ContextMenu from '$lib/layout/ContextMenu.svelte';
	import Modal from '$lib/layout/Modal.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import {
		ICONS,
		IconBellAlert,
		IconChartBar,
		IconChatBubbleLeft,
		IconChevronRight,
		IconDocumentDuplicate,
		IconEllipsisVertical,
		IconExclamationTriangle,
		IconPencilSquare,
		IconPlus,
		IconStar,
		IconTag,
		IconTrash
	} from '@casterlabs/heroicons-svelte';
	import { Button } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	const TAGS = {
		// @ts-ignore
		[null]: 'co.casterlabs.caffeinated.app.page.widgets.tag.none',
		red: 'co.casterlabs.caffeinated.app.page.widgets.tag.red',
		green: 'co.casterlabs.caffeinated.app.page.widgets.tag.green',
		blue: 'co.casterlabs.caffeinated.app.page.widgets.tag.blue'
	};

	const CATEGORY_ICONS = {
		ALERTS: IconBellAlert,
		LABELS: IconTag,
		INTERACTION: IconChatBubbleLeft,
		GOALS: IconChartBar,
		OTHER: IconStar
	};

	const allWidgets = storify(AppPlugins, 'widgets').readable<Awaited<typeof AppPlugins.widgets>>();
	const features = storify(Koi, 'features').readable<Awaited<typeof Koi.features>>();

	let widgets = $derived(($allWidgets || []).filter((w) => w.details.type == 'WIDGET'));
	let supportedFeatures = $derived.by(() => {
		const allFeatures = [];
		for (const fts of Object.values($features || {})) {
			allFeatures.push(...fts);
		}
		return allFeatures;
	});

	let creatableWidgetsByCategory: Record<
		string,
		Array<{
			name: string;
			requiredFeatures: Awaited<typeof AppPlugins.creatableWidgets>[number]['requiredFeatures'];
			create: () => Promise<void>;
		}>
	> = $state({
		ALERTS: [],
		LABELS: [],
		INTERACTION: [],
		GOALS: [],
		OTHER: []
	});

	let showCreationWarningFeaturesModalFor: null | (typeof creatableWidgetsByCategory)[string][number] = $state(null);

	let showingCreateModal = $state(false);
	let createModalCategory: string | null = $state(null);

	let showContextMenuFor = $state<string | null>(null);
	let contextMenu: ContextMenu;

	onMount(() => {
		AppPlugins.creatableWidgets.then((creatableWidgets) => {
			for (const creatable of creatableWidgets) {
				if (creatable.type != 'WIDGET') continue;

				creatableWidgetsByCategory[creatable.category].push({
					name: creatable.friendlyName,
					requiredFeatures: creatable.requiredFeatures,
					create: async () => {
						const id = await AppPlugins.createNewWidget(
							creatable.namespace,
							`${render(creatable.friendlyName)} ${render('co.casterlabs.caffeinated.app.page.widgets.create.new')}`
						);
						goto(`/$caffeinated-sdk-root$/widgets/${id}`);
					}
				});
			}

			creatableWidgetsByCategory = creatableWidgetsByCategory; // re-render
			console.debug('Creatable widgets:', creatableWidgetsByCategory);
		});
	});
</script>

<ContextMenu
	bind:this={contextMenu}
	onclose={() => (showContextMenuFor = null)}
	items={[
		{
			action: () => {
				goto(`/$caffeinated-sdk-root$/widgets/${showContextMenuFor!}`);
			},
			label: 'co.casterlabs.caffeinated.app.page.widgets.edit_widget',
			icon: IconPencilSquare
		},
		// { // TODO tags
		// 	action: () => {
		// 		console.debug('User selected:', widget.tag, tag);
		// 		AppPlugins.assignTag(showContextMenuFor!, tag);
		// 	},
		// 	label: 'co.casterlabs.caffeinated.app.page.widgets.assign_tag',
		// 	icon: IconTag
		// },
		{
			action: () => {
				AppPlugins.copyWidgetUrl(showContextMenuFor!);
			},
			label: 'co.casterlabs.caffeinated.app.page.widgets.copy_link',
			icon: IconDocumentDuplicate
		},
		{
			action: () => {
				AppPlugins.deleteWidget(showContextMenuFor!);
			},
			label: 'co.casterlabs.caffeinated.app.page.widgets.delete',
			icon: IconTrash
		}
	]}
/>

<div class="grid gap-4 grid-cols-2 lg:grid-cols-3 2xl:grid-cols-5">
	{#each widgets as widget}
		{@const WidgetIcon = (ICONS as any)[widget.details.icon] as (typeof ICONS)['academic-cap']}

		<Button
			onclick={() => goto(`/$caffeinated-sdk-root$/widgets/${widget.id}`)}
			oncontextmenu={(e) => {
				showContextMenuFor = widget.id;
				contextMenu.spawn(e);
			}}
		>
			<div class="p-3 flex items-center justify-start space-x-4">
				<WidgetIcon class="w-8 h-8" theme="solid" />

				<span class="w-full whitespace-nowrap text-ellipsis overflow-hidden text-sm font-medium text-left">{widget.name}</span>

				<Button
					borderless
					onclick={(e) => {
						showContextMenuFor = widget.id;
						contextMenu.spawn(e);
					}}
				>
					<IconEllipsisVertical theme="mini" />
					<span class="sr-only">Widget Options</span>
				</Button>
			</div>
		</Button>
	{/each}

	<Button
		class="opacity-80 hover:opacity-100 transition-opacity"
		onclick={() => {
			createModalCategory = null;
			showingCreateModal = true;
		}}
	>
		<div class="p-3 flex items-center justify-center">
			<IconPlus />
			<span class="sr-only">Add New Widget</span>
		</div>
	</Button>
</div>

{#if showCreationWarningFeaturesModalFor}
	{@const listOfPlatforms = Object.keys($features || {})
		.map((p) => render(`co.casterlabs.caffeinated.app.platform.${p}`)) // KICK -> Kick
		.join(', ')}

	{#snippet title()}
		<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.info.widget_features_not_supported.modal.title" />
	{/snippet}

	<Modal {title} onclose={() => (showCreationWarningFeaturesModalFor = null)}>
		<p>
			<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.info.widget_features_not_supported.modal.content" args={{ platform: listOfPlatforms }} />
		</p>

		<div class="mt-8 flex justify-center space-x-1">
			<Button
				onclick={() => {
					showCreationWarningFeaturesModalFor = null;
				}}
			>
				<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.info.widget_features_not_supported.modal.content.cancel" />
			</Button>
			<Button
				onclick={() => {
					showCreationWarningFeaturesModalFor!.create();
					showCreationWarningFeaturesModalFor = null;
					showingCreateModal = false;
				}}
			>
				<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.info.widget_features_not_supported.modal.content.create_anyway" />
			</Button>
		</div>
	</Modal>
{/if}

{#if showingCreateModal && !showCreationWarningFeaturesModalFor}
	{#snippet title()}
		<div class="flex items-center justify-center">
			{#if createModalCategory}
				<button onclick={() => (createModalCategory = null)} class="underline">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.create" />
				</button>

				<IconChevronRight theme="micro" />

				<LocalizedText key={`co.casterlabs.caffeinated.app.page.widgets.create.category.${createModalCategory}`} />
			{:else}
				<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.create" />
			{/if}
		</div>
	{/snippet}

	<Modal {title} onclose={() => (showingCreateModal = false)}>
		{#if createModalCategory}
			{#each creatableWidgetsByCategory[createModalCategory] as creatable}
				{@const isSupported =
					creatable.requiredFeatures // Filter the list of feature, looking for any that aren't in the list.
						.filter((f) => !supportedFeatures.includes(f)).length == 0}

				<Button
					borderless
					class="block w-full h-10"
					onclick={() => {
						if (isSupported) {
							creatable.create();
						} else {
							showCreationWarningFeaturesModalFor = creatable;
						}
					}}
				>
					<div class="truncate text-left p-2 text-sm w-full" class:text-base-11={!isSupported} class:text-base-12={isSupported}>
						{#if !isSupported}
							<IconExclamationTriangle class="inline-block h-4 w-4" />
						{/if}
						<LocalizedText key={creatable.name} />
					</div>
				</Button>
			{/each}
		{:else}
			<div class="grid gap-2 grid-cols-2">
				{#each Object.entries(creatableWidgetsByCategory) as [category, creatableWidgets]}
					{#if creatableWidgets.length > 0}
						{@const CategoryIcon = CATEGORY_ICONS[category as keyof typeof CATEGORY_ICONS] || IconEllipsisVertical}

						<Button onclick={() => (createModalCategory = category)}>
							<div class="p-4 flex flex-col items-center justify-center space-y-2">
								<CategoryIcon class="w-10 h-10" theme="solid" />

								<span class="text-sm font-medium text-center">
									<LocalizedText key={`co.casterlabs.caffeinated.app.page.widgets.create.category.${category}`} />
								</span>
							</div>
						</Button>
					{/if}
				{/each}
			</div>
		{/if}
	</Modal>
{/if}

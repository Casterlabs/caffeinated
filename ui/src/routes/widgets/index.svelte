<script>
	import Modal from '../../components/layout/Modal.svelte';
	import PageTitle from '../../components/PageTitle.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';
	import CardList from '../../components/ui/CardList/index.svelte';
	import Card from '../../components/ui/cardlist/Card.svelte';
	import FocusListener from '../../components/interaction/FocusListener.svelte';

	import { onMount } from 'svelte';
	import { t } from '$lib/translate.mjs';

	const CATEGORY_ICONS = {
		ALERTS: 'icon/bell-alert',
		LABELS: 'icon/tag',
		INTERACTION: 'icon/chat-bubble-oval-left',
		GOALS: 'icon/chart-bar',
		OTHER: 'icon/star'
	};

	let widgets = [];
	let showingTutorialModal = false;

	let widgetCreationPopupVisible = false;

	let widgetCategories = {};

	async function reload() {
		widgets = (await Caffeinated.plugins.widgets).filter((w) => w.details.type == 'WIDGET');
	}

	onMount(async () => {
		reload();

		let _widgetCategories = {
			ALERTS: [],
			LABELS: [],
			INTERACTION: [],
			GOALS: [],
			OTHER: []
		};

		const creatableWidgets = await Caffeinated.plugins.creatableWidgets;
		for (const creatable of creatableWidgets) {
			if (creatable.type == 'WIDGET') {
				_widgetCategories[creatable.category].push({
					name: creatable.friendlyName,
					create: () => {
						Caffeinated.plugins.createNewWidget(
							creatable.namespace,
							`${t(creatable.friendlyName)} ${t('page.widgets.create.new')}`
						);
					}
				});
			}
		}

		// This forces svelte to rerender.
		widgetCategories = _widgetCategories;
	});
</script>

<PageTitle title="page.widgets" />

<div>
	<LocalizedText key="page.widgets.info" />

	<button
		class="flex flex-row items-center text-link"
		on:click={() => (showingTutorialModal = true)}
	>
		<icon class="inline-block w-4" data-icon="icon/chevron-right" />
		<LocalizedText key="show_me_how" />
	</button>
</div>

{#if showingTutorialModal}
	<Modal on:close={() => (showingTutorialModal = false)}>
		<LocalizedText slot="title" key="page.widgets.info.show_me_how.modal.title" />

		<!-- svelte-ignore a11y-media-has-caption -->
		<video src="/tutorials/create_obs_source_widget.webm" autoplay loop muted />
	</Modal>
{/if}

<div class="mt-8">
	<CardList>
		{#each widgets as widget}
			<Card icon={widget.details.icon} text={widget.name} href="/widgets/edit?id={widget.id}">
				<div class="absolute inset-y-1 right-2.5 flex items-center space-x-1">
					<button
						class="text-base-12 hover:text-base-11"
						title={t('sr.page.widgets.copy_link')}
						on:click|stopPropagation={() => {
							window.Caffeinated.plugins.copyWidgetUrl(widget.id);
						}}
					>
						<span class="sr-only">
							<LocalizedText key="sr.page.widgets.copy_link" />
						</span>
						<icon class="w-5 h-5" data-icon="icon/document-duplicate" />
					</button>

					<button
						class="text-red-500 hover:text-red-600"
						title={t('sr.page.widgets.delete')}
						on:click|stopPropagation={() => {
							window.Caffeinated.plugins.deleteWidget(widget.id);
							reload();
						}}
					>
						<span class="sr-only">
							<LocalizedText key="sr.page.widgets.delete" />
						</span>
						<icon class="w-5 h-5" data-icon="icon/trash" />
					</button>
				</div>
			</Card>
		{/each}
	</CardList>

	<button
		class="fixed bottom-3 -translate-x-3 flex items-center justify-center shadow-sm rounded-lg border border-base-5 bg-base-1 p-2 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 overflow-hidden hover:overflow-visible"
		aria-haspopup="true"
		aria-controls="widget-creation-dropup"
		title={t('sr.page.widgets.create')}
	>
		<div class="absolute left-0 bottom-full -translate-x-px" id="widget-creation-dropup">
			<div
				class="mb-1.5 shadow-sm rounded-lg border border-base-5 bg-base-1 w-36 divide-y divide-current text-base-5"
			>
				{#each Object.entries(widgetCategories) as [category, widgets]}
					<button
						class="relative block w-full h-10 overflow-hidden hover:overflow-visible"
						aria-haspopup="true"
						aria-controls="widget-creation-dropright-{category}"
					>
						<div class="p-2 flex flex-row items-center text-md space-x-2 text-base-12 w-full">
							<icon class="flex-0 w-5 h-5" data-icon={CATEGORY_ICONS[category]} />
							<span class="flex-1 text-left">
								<LocalizedText key="page.widgets.create.category.{category}" />
							</span>
							<icon class="flex-0 w-5 h-5" data-icon="icon/chevron-right" />
						</div>

						<div class="absolute left-full bottom-0 w-96">
							<div
								class="translate-x-1.5 translate-y-px shadow-sm rounded-lg border border-base-5 bg-base-1 w-36 divide-y divide-current text-base-5"
								id="widget-creation-dropright-{category}"
							>
								{#each widgets as widget}
									<button
										class="block w-full h-10"
										title={t(widget.name)}
										on:click={() => {
											widget.create();
										}}
									>
										<div class="truncate text-left p-2 text-sm text-base-12 w-full">
											<LocalizedText key={widget.name} />
										</div>
									</button>
								{/each}
							</div>
						</div>
					</button>
				{/each}
			</div>
		</div>

		<div class="text-base-12">
			<span class="sr-only">
				<LocalizedText key="sr.page.widgets.create" />
			</span>
			<icon data-icon="icon/plus" />
		</div>
	</button>
</div>

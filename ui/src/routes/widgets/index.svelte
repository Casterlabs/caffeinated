<script>
	import Modal from '$lib/ui/Modal.svelte';
	import PageTitle from '../../components/PageTitle.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import CardList from '$lib/ui/CardList/index.svelte';
	import Card from '$lib/ui/CardList/Card.svelte';

	import { onMount } from 'svelte';
	import { t } from '$lib/translate.mjs';
	import { STREAMING_SERVICE_NAMES } from '../../components/caffeinatedAuth.mjs';
	import Button from '$lib/ui/Button.svelte';

	const CATEGORY_ICONS = {
		ALERTS: 'icon/bell-alert',
		LABELS: 'icon/tag',
		INTERACTION: 'icon/chat-bubble-oval-left',
		GOALS: 'icon/chart-bar',
		OTHER: 'icon/star'
	};

	let widgets = [];
	let creatableWidgetByCategory = {
		ALERTS: [],
		LABELS: [],
		INTERACTION: [],
		GOALS: [],
		OTHER: []
	};
	let supportedFeatures = [];
	let signedInPlatforms = [];

	let showingTutorialModal = false;
	let showCreationWarningFeaturesModalFor = null;

	async function refreshWidgetsList() {
		widgets = (await Caffeinated.plugins.widgets) //
			.filter((w) => w.details.type == 'WIDGET');
	}

	onMount(async () => {
		refreshWidgetsList();

		Caffeinated.koi.features.then((featuresByPlatform) => {
			signedInPlatforms = Object.keys(featuresByPlatform);

			for (const features of Object.values(featuresByPlatform)) {
				features.forEach((f) => supportedFeatures.push(f));
			}

			supportedFeatures = supportedFeatures; // re-render
			console.debug('Supported features:', supportedFeatures);
		});

		Caffeinated.plugins.creatableWidgets.then((creatableWidgets) => {
			for (const creatable of creatableWidgets) {
				if (creatable.type != 'WIDGET') continue;

				creatableWidgetByCategory[creatable.category].push({
					name: creatable.friendlyName,
					requiredFeatures: creatable.requiredFeatures,
					create: () => {
						Caffeinated.plugins.createNewWidget(
							creatable.namespace,
							`${t(creatable.friendlyName)} ${t('page.widgets.create.new')}`
						);
					}
				});
			}

			creatableWidgetByCategory = creatableWidgetByCategory; // re-render
			console.debug('Creatable widgets:', creatableWidgetByCategory);
		});
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

		<img src="/$caffeinated-sdk-root$/tutorials/create_obs_source_widget.gif" alt="" />
	</Modal>
{/if}

{#if showCreationWarningFeaturesModalFor}
	{@const listOfPlatforms = (() => {
		// Dirty AF. Just trying to make it gramatically correct.
		const commaSeparated = signedInPlatforms
			.map((p) => STREAMING_SERVICE_NAMES[p]) // KICK -> Kick
			.join(', '); // "Kick, Twitch"

		const splitByLastComma = commaSeparated.split(/, [A-Za-z]+$/); // ["Kick", ", Twitch"]
		if (splitByLastComma.length == 1) return commaSeparated;

		let result = splitByLastComma[0]; // "Kick"
		result += ' or ';
		result += splitByLastComma[1].substring(', '.length);
		return result;
	})()}

	<Modal
		on:close={() => {
			showCreationWarningFeaturesModalFor = null;
		}}
	>
		<LocalizedText slot="title" key="page.widgets.info.widget_features_not_supported.modal.title" />

		<p>
			<LocalizedText
				key="page.widgets.info.widget_features_not_supported.modal.content"
				opts={{ platform: listOfPlatforms }}
			/>
		</p>

		<div class="mt-8">
			<Button
				on:click={() => {
					showCreationWarningFeaturesModalFor = null;
				}}
			>
				<LocalizedText key="page.widgets.info.widget_features_not_supported.modal.content.cancel" />
			</Button>
			<Button
				on:click={() => {
					showCreationWarningFeaturesModalFor.create();
					showCreationWarningFeaturesModalFor = null;
				}}
			>
				<LocalizedText
					key="page.widgets.info.widget_features_not_supported.modal.content.create_anyway"
				/>
			</Button>
		</div>
	</Modal>
{/if}

<div class="mt-8">
	<CardList>
		{#each widgets as widget}
			<Card
				icon={widget.details.icon}
				text={widget.name}
				href="/$caffeinated-sdk-root$/widgets/edit?id={widget.id}"
			>
				<div class="text-right flex items-center space-x-1">
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
						class="text-error hover:opacity-80"
						title={t('sr.page.widgets.delete')}
						on:click|stopPropagation={() => {
							window.Caffeinated.plugins.deleteWidget(widget.id);
							refreshWidgetsList();
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
				{#each Object.entries(creatableWidgetByCategory) as [category, widgets]}
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
									{@const isSupported =
										widget.requiredFeatures // Filter the list of feature, looking for any that aren't in the list.
											.filter((f) => !supportedFeatures.includes(f)).length == 0}
									<button
										class="block w-full h-10"
										title={t(widget.name)}
										on:click={() => {
											if (isSupported) {
												widget.create();
											} else {
												showCreationWarningFeaturesModalFor = widget;
											}
										}}
									>
										<div
											class="truncate text-left p-2 text-sm  w-full"
											class:text-base-11={!isSupported}
											class:text-base-12={isSupported}
										>
											{#if !isSupported}
												<icon
													class="inline-block h-4 w-4 translate-y-0.5"
													data-icon="icon/exclamation-circle"
												/>
											{/if}
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

<script>
	import Modal from '../../components/layout/Modal.svelte';
	import PageTitle from '../../components/PageTitle.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';
	import CardList from '../../components/ui/CardList/index.svelte';
	import Card from '../../components/ui/cardlist/Card.svelte';

	import { onMount } from 'svelte';
	import { t } from '$lib/translate.mjs';

	let widgets = [];
	let showingTutorialModal = false;
	let showingCreationModal = false;

	onMount(async () => {
		widgets = (await Caffeinated.plugins.widgets).filter((w) => w.details.type == 'WIDGET');
	});
</script>

<PageTitle title="page.widgets" />

<div>
	<LocalizedText key="page.widgets.info" />

	<button
		class="flex flex-row items-center text-link"
		on:click={() => (showingTutorialModal = true)}
	>
		<icon class="inline-block w-4" data-icon="filled/chevron-right" />
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

{#if showingCreationModal}
	<Modal on:close={() => (showingCreationModal = false)}>
		<LocalizedText slot="title" key="page.widgets.create.modal.title" />

		// TODO Pick from category dropdown, pick from list, give it a name.
	</Modal>
{/if}

<div class="mt-8">
	<CardList>
		{#each widgets as widget}
			<Card
				icon={widget.details.icon}
				text={widget.details.friendlyName}
				href="/widgets/edit?id={widget.id}"
			/>
		{/each}

		<button
			class="relative flex items-center justify-center rounded-lg border border-mauve-5 bg-mauve-1 p-5 focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7"
			title={t('sr.page.widgets.create')}
			on:click={() => (showingCreationModal = true)}
		>
			<div class="text-mauve-12">
				<span class="sr-only">
					<LocalizedText key="sr.page.widgets.create" />
				</span>
				<icon data-icon="filled/plus" />
			</div>
		</button>
	</CardList>
</div>

<script>
	import Modal from '$lib/ui/Modal.svelte';
	import PageTitle from '../components/PageTitle.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import CardList from '$lib/ui/CardList/index.svelte';
	import Card from '$lib/ui/cardlist/Card.svelte';

	import { t } from '$lib/translate.mjs';
	import { onMount } from 'svelte';

	let docks = [];
	let showingTutorialModal = false;

	function copyWidgetUrl(id) {
		Caffeinated.plugins.copyWidgetUrl(id);
	}

	onMount(async () => {
		docks = (await Caffeinated.plugins.widgets).filter((w) => w.details.type == 'DOCK');
	});
</script>

<PageTitle title="page.docks" />

<div>
	<LocalizedText key="page.docks.info" />

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
		<LocalizedText slot="title" key="page.docks.info.show_me_how.modal.title" />

		<!-- svelte-ignore a11y-media-has-caption -->
		<video src="/tutorials/create_obs_browser_dock.webm" autoplay loop muted />
	</Modal>
{/if}

<div class="mt-8">
	<CardList>
		{#each docks as dock}
			<Card
				icon={dock.details.icon}
				text={dock.details.friendlyName}
				on:click={() => copyWidgetUrl(dock.id)}
			>
				<div class="absolute inset-y-1 right-2.5 flex items-center space-x-1">
					<button
						class="text-base-12 hover:text-base-11"
						title={t('sr.page.widgets.copy_link')}
						on:click|stopPropagation={() => {
							window.Caffeinated.plugins.copyWidgetUrl(dock.id);
						}}
					>
						<span class="sr-only">
							<LocalizedText key="sr.page.widgets.copy_link" />
						</span>
						<icon class="w-5 h-5" data-icon="icon/document-duplicate" />
					</button>
				</div></Card
			>
		{/each}
	</CardList>
</div>

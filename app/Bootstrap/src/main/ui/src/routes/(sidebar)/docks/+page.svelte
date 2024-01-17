<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import Modal from '$lib/ui/Modal.svelte';
	import PageTitle from '$lib/layout/PageTitle.svelte';
	import CardList from '$lib/ui/CardList/index.svelte';
	import Card from '$lib/ui/CardList/Card.svelte';

	import { onMount } from 'svelte';

	let docks = [];
	let showingTutorialModal = false;

	function copyWidgetUrl(id) {
		Caffeinated.pluginIntegration.copyWidgetUrl(id);
	}

	onMount(async () => {
		docks = (await Caffeinated.pluginIntegration.widgets).filter((w) => w.details.type == 'DOCK');
	});
</script>

<PageTitle title="co.casterlabs.caffeinated.app.page.docks" />

<div>
	<LocalizedText key="co.casterlabs.caffeinated.app.page.docks.info" />

	<button
		class="flex flex-row items-center text-link"
		on:click={() => (showingTutorialModal = true)}
	>
		<icon class="inline-block w-4" data-icon="icon/chevron-right" />
		<LocalizedText key="co.casterlabs.caffeinated.app.tutorial.show_me_how" />
	</button>
</div>

{#if showingTutorialModal}
	<Modal on:close={() => (showingTutorialModal = false)}>
		<LocalizedText
			slot="title"
			key="co.casterlabs.caffeinated.app.page.docks.info.show_me_how.modal.title"
		/>

		<img src="/$caffeinated-sdk-root$/tutorials/create_obs_browser_dock.gif" alt="" />
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
				<div class="text-right flex items-center space-x-1">
					<button
						class="text-base-12 hover:text-base-11"
						on:click|stopPropagation={() => {
							window.Caffeinated.pluginIntegration.copyWidgetUrl(dock.id);
						}}
					>
						<LocalizedProperty
							key="co.casterlabs.caffeinated.app.page.widgets.copy_link"
							property="title"
						/>
						<span class="sr-only">
							<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.copy_link" />
						</span>
						<icon class="w-5 h-5" data-icon="icon/document-duplicate" />
					</button>
				</div></Card
			>
		{/each}
	</CardList>
</div>

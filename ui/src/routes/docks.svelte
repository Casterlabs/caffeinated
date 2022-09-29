<script>
	import Modal from '../components/layout/Modal.svelte';
	import PageTitle from '../components/PageTitle.svelte';
	import LocalizedText from '../components/LocalizedText.svelte';
	import CardList from '../components/ui/CardList/index.svelte';
	import Card from '../components/ui/cardlist/Card.svelte';

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
		<icon class="inline-block w-4" data-icon="filled/chevron-right" />
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
			/>
		{/each}
	</CardList>
</div>

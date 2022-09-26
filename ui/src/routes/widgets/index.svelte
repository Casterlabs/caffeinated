<script>
	import Modal from '../../components/layout/Modal.svelte';
	import PageTitle from '../../components/PageTitle.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';

	import { onMount } from 'svelte';

	let docks = [];
	let showingTutorialModal = false;

	onMount(async () => {
		docks = (await Caffeinated.plugins.widgets).filter((w) => w.details.type == 'WIDGET');
	});
</script>

<PageTitle title="page.widgets" />

<div>
	<LocalizedText key="page.widgets.info" />

	<button
		class="flex flex-row items-center text-link"
		on:click={() => (showingTutorialModal = true)}
	>
		<icon class="inline-block w-4" data-icon="chevron-right" />
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

<div class="mt-8 grid gap-4 grid-cols-2 lg:grid-cols-3 2xl:grid-cols-5">
	{#each docks as dock}
		<a
			class="relative flex items-center space-x-3 rounded-lg border border-mauve-6 bg-mauve-2 p-5 shadow-sm focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7"
			href="/widgets/edit?id={dock.id}"
		>
			<div class="flex-shrink-0 text-mauve-12">
				<icon data-icon={dock.details.icon} />
			</div>
			<div class="min-w-0 flex-1">
				<p class="text-sm font-medium text-mauve-12 text-left">
					{dock.details.friendlyName}
				</p>
			</div>
		</a>
	{/each}
</div>

<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import PageTitle from '../../components/PageTitle.svelte';

	import { page } from '$app/stores';
	import { onMount } from 'svelte';

	const preferences = st || Caffeinated.UI.svelte('preferences');

	let loadedWidgets = false;

	// prettier-ignore
	const tabs = [
		['page.settings.appearance', '/$caffeinated-sdk-root$/settings'],
		['page.settings.accounts',   '/$caffeinated-sdk-root$/settings/accounts'],
		['page.settings.plugins',    '/$caffeinated-sdk-root$/settings/plugins'],
		['page.settings.about',      '/$caffeinated-sdk-root$/settings/about'],
		['Developer Stuff',          '/$caffeinated-sdk-root$/settings/developer-stuff']
	];

	// Filter the list of tabs for a match.
	$: currentPage = (tabs.filter(
		([_, href]) => $page.url.pathname /*+ $page.url.search*/ == href
	)[0] || [])[0];

	onMount(() => {
		// Caffeinated.plugins.widgets
		// 	.then((widgets) => widgets.filter((w) => w.details.type == 'SETTINGS_APPLET'))
		// 	.then((settingsApplets) => {
		// 		for (const settingsApplet of settingsApplets) {
		// 			tabs.push([
		// 				settingsApplet.details.friendlyName,
		// 				'/$caffeinated-sdk-root$/settings/applet?id=' + settingsApplet.id
		// 			]);
		// 		}
		// 		loadedWidgets = true;
		// 	});
	});
</script>

<PageTitle title={['page.settings', currentPage]} />

<!-- <h1 class="text-3xl font-bold tracking-tight text-base-12 mb-6">Settings</h1> -->

<div class="border-b border-base-8">
	<nav class="-mb-px flex space-x-8 w-full overflow-auto">
		{#key loadedWidgets}
			{#each tabs as [name, href]}
				{@const isSelected = currentPage == name}
				<a
					{href}
					class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
					class:hidden={name == 'Developer Stuff' && !$preferences?.enableStupidlyUnsafeSettings}
					aria-current={isSelected ? 'page' : undefined}
					class:border-b-2={isSelected}
					class:text-primary-11={isSelected}
				>
					<LocalizedText key={name} />
				</a>
			{/each}
		{/key}
	</nav>
</div>

<div class="mt-6">
	<slot />
</div>

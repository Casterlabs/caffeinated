<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import PageTitle from '../../components/PageTitle.svelte';

	import { page } from '$app/stores';

	const preferences = st || Caffeinated.UI.svelte('preferences');

	// prettier-ignore
	const tabs = [
		['page.settings.appearance', '/settings/appearance'],
		['page.settings.accounts',   '/settings/accounts'],
		['page.settings.plugins',    '/settings/plugins'],
		['page.settings.about',      '/settings/about'],
		['Developer Stuff',          '/settings/developer-stuff']
	];

	// Filter the list of tabs for a match.
	$: currentPage = tabs.filter(([_, href]) => $page.url.pathname == href)[0][0];
</script>

<PageTitle title={['page.settings', currentPage]} />

<!-- <h1 class="text-3xl font-bold tracking-tight text-base-12 mb-6">Settings</h1> -->

<div class="border-b border-base-8">
	<nav class="-mb-px flex space-x-8">
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
	</nav>
</div>

<div class="mt-6">
	<slot />
</div>

<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import PageTitle from '../../components/PageTitle.svelte';

	import { page } from '$app/stores';

	const preferences = st || Caffeinated.UI.svelte('preferences');

	// prettier-ignore
	const tabs = [
		['page.settings.appearance', '/settings/appearance'],
		['page.settings.plugins',    '/settings/plugins'],
		['page.settings.accounts',   '/settings/accounts'],
		['page.settings.about',      '/settings/about']
	];

	// Filter the list of tabs for a match.
	// We default on "Developer Stuff" for... reasons...
	$: currentPage = (tabs.filter(([_, href]) => $page.url.pathname == href)[0] || [
		'Developer Stuff'
	])[0];
</script>

<PageTitle title={['page.settings', currentPage]} />

<!-- <h1 class="text-3xl font-bold tracking-tight text-mauve-12 mb-6">Settings</h1> -->

<div class="border-b border-mauve-8">
	<nav class="-mb-px flex space-x-8">
		{#each tabs as [name, href]}
			{@const isSelected = currentPage == name}
			<a
				{href}
				class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
				aria-current={isSelected ? 'page' : undefined}
				class:border-b-2={isSelected}
				class:text-crimson-11={isSelected}
			>
				<LocalizedText key={name} />
			</a>
		{/each}

		{#if $preferences?.enableStupidlyUnsafeSettings}
			{@const href = '/settings/developer-stuff'}
			{@const isSelected = $page.url.pathname == href}

			<a
				{href}
				class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
				aria-current={isSelected ? 'page' : undefined}
				class:border-b-2={isSelected}
				class:text-crimson-11={isSelected}
			>
				Developer Stuff
			</a>
		{/if}
	</nav>
</div>

<div class="mt-6">
	<slot />
</div>

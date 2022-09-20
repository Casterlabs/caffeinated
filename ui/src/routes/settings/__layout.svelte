<script>
	import LocalizedText from '../../components/LocalizedText.svelte';

	import { page } from '$app/stores';

	const tabs = [
		['page.settings.appearance', '/settings/appearance'],
		['page.settings.plugins', '/settings/plugins'],
		['page.settings.accounts', '/settings/accounts'],
		['page.settings.about', '/settings/about']
		// ['Unsafe', '/settings/developer-stuff']
	];
</script>

<!-- <h1 class="text-3xl font-bold tracking-tight text-mauve-12 mb-6">Settings</h1> -->

<div class="border-b border-current text-mauve-11">
	<nav class="-mb-px flex space-x-8">
		{#each tabs as [name, href]}
			{@const isSelected = $page.url.pathname == href}

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
	</nav>
</div>

<div class="mt-6">
	<slot />
</div>

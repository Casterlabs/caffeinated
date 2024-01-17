<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import PageTitle from '$lib/layout/PageTitle.svelte';

	import { page } from '$app/stores';

	// prettier-ignore
	const tabs = [
		['co.casterlabs.caffeinated.app.page.chat_bot.commands', '/$caffeinated-sdk-root$/chat-bot/commands'],
		['co.casterlabs.caffeinated.app.page.chat_bot.shouts',   '/$caffeinated-sdk-root$/chat-bot/shouts'],
		['co.casterlabs.caffeinated.app.page.chat_bot.timers',   '/$caffeinated-sdk-root$/chat-bot/timers'],
		['co.casterlabs.caffeinated.app.page.chat_bot.settings', '/$caffeinated-sdk-root$/chat-bot/settings']
	];

	// Filter the list of tabs for a match.
	$: currentPage = tabs.filter(([_, href]) => $page.url.pathname == href)[0]?.[0];
</script>

<PageTitle title={['co.casterlabs.caffeinated.app.page.chat_bot', currentPage]} />

<!-- <h1 class="text-3xl font-bold tracking-tight text-base-12 mb-6">Settings</h1> -->

<div class="border-b border-base-8">
	<nav class="-mb-px flex space-x-8">
		{#each tabs as [name, href]}
			{@const isSelected = currentPage == name}
			<a
				{href}
				class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
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

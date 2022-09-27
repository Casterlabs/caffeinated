<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import PageTitle from '../../components/PageTitle.svelte';

	import { page } from '$app/stores';

	// prettier-ignore
	const tabs = [
		['page.chat_bot.commands', '/chat-bot/commands'],
		['page.chat_bot.shouts',   '/chat-bot/shouts'],
		['page.chat_bot.timers',   '/chat-bot/timers'],
		['page.chat_bot.settings', '/chat-bot/settings']
	];

	// Filter the list of tabs for a match.
	$: currentPage = tabs.filter(([_, href]) => $page.url.pathname == href)[0][0];
</script>

<PageTitle title={['page.chat_bot', currentPage]} />

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
	</nav>
</div>

<div class="mt-6">
	<slot />
</div>

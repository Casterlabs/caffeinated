<script lang="ts">
	import { page } from '$app/state';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';

	// prettier-ignore
	const tabs = [
		['co.casterlabs.caffeinated.app.page.chat_bot.commands', '/$caffeinated-sdk-root$/chat-bot/commands'],
		['co.casterlabs.caffeinated.app.page.chat_bot.shouts',   '/$caffeinated-sdk-root$/chat-bot/shouts'],
		['co.casterlabs.caffeinated.app.page.chat_bot.timers',   '/$caffeinated-sdk-root$/chat-bot/timers'],
		['co.casterlabs.caffeinated.app.page.chat_bot.settings', '/$caffeinated-sdk-root$/chat-bot/settings']
	];
</script>

<div class="border-b border-base-8 -mx-4 px-4">
	<nav class="-mb-px flex space-x-4 w-full overflow-auto">
		{#each tabs as [name, href]}
			{@const isSelected = page.url.pathname == href}
			<a
				{href}
				class="border-current whitespace-nowrap pb-4 font-medium text-sm"
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

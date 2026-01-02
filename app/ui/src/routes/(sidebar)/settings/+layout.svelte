<script lang="ts">
	import { page } from '$app/state';
	import { storify } from '$lib/bridgeHelper';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';

	// prettier-ignore
	const tabs = [
		['co.casterlabs.caffeinated.app.page.settings.appearance', '/$caffeinated-sdk-root$/settings/appearance'],
		['co.casterlabs.caffeinated.app.page.settings.accounts',   '/$caffeinated-sdk-root$/settings/accounts'],
		['co.casterlabs.caffeinated.app.page.settings.plugins',    '/$caffeinated-sdk-root$/settings/plugins'],
		['co.casterlabs.caffeinated.app.page.settings.about',      '/$caffeinated-sdk-root$/settings/about'],
		['Developer Stuff',                                        '/$caffeinated-sdk-root$/settings/developer-stuff']
	];

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
</script>

<div class="border-b border-base-8 -mx-4 px-4">
	<nav class="-mb-px flex space-x-4 w-full overflow-auto">
		{#each tabs as [name, href]}
			{@const isSelected = page.url.pathname == href}
			<a
				{href}
				class="border-current whitespace-nowrap pb-4 font-medium text-sm"
				class:hidden={name == 'Developer Stuff' && !$uiPreferences?.enableStupidlyUnsafeSettings}
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

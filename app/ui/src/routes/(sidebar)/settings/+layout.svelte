<script lang="ts">
	import { page } from '$app/state';
	import { storify } from '$lib/bridge-helper';

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
	const allWidgets = storify(AppPlugins, 'widgets').readable<Awaited<typeof AppPlugins.widgets>>();
	let settingsApplets = $derived(($allWidgets || []).filter((w) => w.details.type == 'SETTINGS_APPLET'));
</script>

<div class="h-full flex flex-col space-y-4">
	<div class="border-b border-base-8 -mx-6 px-6">
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
			{#each settingsApplets as applet}
				{@const href = `/$caffeinated-sdk-root$/settings/applet?id=${applet.id}`}
				{@const isSelected = page.url.pathname + page.url.search == href}

				<a
					{href}
					class="border-current whitespace-nowrap pb-4 font-medium text-sm"
					aria-current={isSelected ? 'page' : undefined}
					class:border-b-2={isSelected}
					class:text-primary-11={isSelected}
				>
					<LocalizedText key={applet.details.friendlyName} />
				</a>
			{/each}
		</nav>
	</div>

	<div class="flex-1">
		<!-- svelte-ignore slot_element_deprecated -->
		<slot />
	</div>
</div>

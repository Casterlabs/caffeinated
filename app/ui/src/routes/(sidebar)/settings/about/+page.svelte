<script lang="ts">
	import { themeEffectiveAppearance } from '$lib/appShim';
	import { modify, storify } from '$lib/bridgeHelper';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { Input } from '@casterlabs/ui';

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
</script>

<ul class="bg-base-2 shadow rounded-md border border-base-6">
	<li class="py-4 flex flex-row space-x-8 border-b border-base-6">
		<div class="flex-1 flex items-center">
			<img
				src="/$caffeinated-sdk-root$/images/brand/wordmark/{$uiPreferences?.icon || 'casterlabs'}/{$themeEffectiveAppearance == 'DARK' ? 'white' : 'black'}.svg"
				class="h-auto w-full max-w-60"
				alt="Casterlabs"
			/>
		</div>
		<div class="flex-1 flex flex-col justify-center text-base-11">
			<p>Casterlabs-Caffeinated</p>
			<p>
				{#await App.buildInfo then buildInfo}
					{#if buildInfo.isDev}
						Developer Build
					{:else}
						Version {buildInfo.version}-{buildInfo.commit} ({buildInfo.buildChannel})
					{/if}
				{/await}
			</p>
		</div>
	</li>
	<li class="px-2 py-4 text-base-11">
		<LocalizedText key="co.casterlabs.caffeinated.app.love" />
		<br />
		<LocalizedText key="co.casterlabs.caffeinated.app.copyright" args={{ year: new Date().getFullYear() }} />

		<br />
		<br />
		<label>
			<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.about.enable_unsafe" />
			<Input
				type="checkbox"
				checked={$uiPreferences?.enableStupidlyUnsafeSettings}
				onchange={(e) => {
					const checked = (e.target as HTMLInputElement).checked;
					modify(AppConfig, 'uiPreferences', 'enableStupidlyUnsafeSettings', checked);
				}}
			/>
		</label>
		<br />
		<label>
			<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.about.enable_alternate_themes" />
			<Input
				type="checkbox"
				checked={$uiPreferences?.enableAlternateThemes}
				onchange={(e) => {
					const checked = (e.target as HTMLInputElement).checked;
					modify(AppConfig, 'uiPreferences', 'enableAlternateThemes', checked);
				}}
			/>
		</label>
	</li>
</ul>

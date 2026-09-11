<script lang="ts">
	import { themeEffectiveAppearance } from '$lib/app-shim';
	import { modify, storify } from '$lib/bridge-helper';

	import LoadingSpinner from '$lib/layout/LoadingSpinner.svelte';

	import { onMount } from 'svelte';

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
	const appPreferences = storify(AppConfig, 'appPreferences').readable<Awaited<typeof AppConfig.appPreferences>>();

	async function resetKoi() {
		await modify(AppConfig, 'appPreferences', 'koiUrl', 'wss://api.casterlabs.co/v2/koi');
		// @ts-ignore
		saucer.messages.emit(['app:restart']);
	}

	let startupProgress = $state(0);
	let startupStep = $state('');

	// @ts-ignore
	window.__handleStartupProgress = function (progress: number, step: string) {
		startupProgress = progress;
		startupStep = step;

		if (progress == 1) {
			AppUI.onUILoaded();
		}
	};

	onMount(() => AppUI.onUILoaded());
</script>

<div class="mt-10 flex flex-col items-center justify-center">
	<div class="w-64">
		<img
			src="/$caffeinated-sdk-root$/images/brand/wordmark/{$uiPreferences?.icon || 'casterlabs'}/{$themeEffectiveAppearance == 'DARK' ? 'white' : 'black'}.svg"
			class="h-auto w-auto"
			alt=""
		/>
	</div>

	<div class="mt-8 w-16">
		<LoadingSpinner />
	</div>

	{#if startupProgress > 0}
		<div class="mt-32 mb-16 text-center text-xs text-base-11">
			<div class="bg-base-9 rounded-sm overflow-hidden w-48 h-1">
				<div class="bg-base-11 h-full" style:width="{startupProgress * 100}%"></div>
			</div>
			<span class="mt-2 block">
				{startupStep}
			</span>
		</div>
	{/if}

	{#if $appPreferences?.koiUrl != 'wss://api.casterlabs.co/v2/koi'}
		<br />
		<small>
			Having connection issues? Try <button class="text-link cursor-pointer" onclick={resetKoi}> switching back to normal Koi. </button>
		</small>
	{/if}
</div>

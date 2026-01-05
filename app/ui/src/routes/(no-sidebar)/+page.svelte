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

	onMount(() => setTimeout(AppUI.onUILoaded, 2000));
</script>

<div class="mt-10 flex flex-col items-center justify-center">
	<div class="w-64">
		<img
			src="/$caffeinated-sdk-root$/images/brand/wordmark/{$uiPreferences?.icon || 'casterlabs'}/{$themeEffectiveAppearance == 'DARK' ? 'white' : 'black'}.svg"
			class="h-auto w-auto"
			alt=""
		/>
	</div>

	<div class="mt-8 mb-16 w-16">
		<LoadingSpinner />
	</div>

	{#if $appPreferences?.koiUrl != 'wss://api.casterlabs.co/v2/koi'}
		<br />
		<small>
			Having connection issues? Try <button class="text-link cursor-pointer" onclick={resetKoi}> switching back to normal Koi. </button>
		</small>
	{/if}
</div>

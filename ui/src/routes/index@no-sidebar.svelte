<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';

	import { onDestroy } from 'svelte';
	import { icon, iconColor } from '$lib/app.mjs';

	const preferences = st || Caffeinated.UI.svelte('preferences');
	const useBetaKoiPath = st || Caffeinated.svelte('useBetaKoiPath');

	let mikeysModeEnabled = false;

	function disableBetaKoi() {
		Caffeinated.useBetaKoiPath = false;
		Bridge.emit('app:restart');
	}

	onDestroy(
		preferences.subscribe((val) => {
			if (!val) return;

			mikeysModeEnabled = val.mikeysMode;
			console.debug("Mikey's Mode:", mikeysModeEnabled);

			setTimeout(
				Caffeinated.UI.onUILoaded,
				mikeysModeEnabled ? (120 + 15) * 1000 /* 2m15s */ : 1500
			);
		})
	);
</script>

<div class="mt-10 flex flex-col items-center justify-center">
	<div class="w-64">
		{#if mikeysModeEnabled}
			<img src="/images/mikeys.png" class="h-auto w-auto" alt="Mikeys Logo" />
		{:else}
			<img
				src="/images/brand/wordmark/{$icon}/{$iconColor}.svg"
				class="h-auto w-auto"
				alt="Casterlabs Logo"
			/>
		{/if}
	</div>

	<div class="mt-8 mb-16 w-16">
		<LoadingSpinner />
	</div>

	{#if mikeysModeEnabled}
		<div id="mikeys">
			Enjoy your
			<a href="https://eatmikeys.com/" target="_blank" class="text-link"> Mikey's </a>
			:^)
		</div>
	{/if}

	{#if $useBetaKoiPath}
		<br />
		<!-- svelte-ignore a11y-missing-attribute -->
		<small>
			Having connection issues? Try <a on:click={disableBetaKoi}>disabling the Koi beta.</a></small
		>
	{/if}
</div>

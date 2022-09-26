<script>
	import LoadingSpinner from '../components/LoadingSpinner.svelte';

	import { onDestroy } from 'svelte';

	const preferences = st || Caffeinated.UI.svelte('preferences');
	const effectiveTheme = st || Caffeinated.themeManager.svelte('effectiveTheme');
	const useBetaKoiPath = st || Caffeinated.svelte('useBetaKoiPath');

	$: iconColor = $effectiveTheme?.appearance == 'LIGHT' ? 'black' : 'white';
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
				mikeysModeEnabled ? (120 + 15) * 1000 /* 2m15s */ : 150
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
				src="/images/wordmark/{$preferences?.icon || 'casterlabs'}/{iconColor}.svg"
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
			<a href="https://eatmikeys.com/" target="_blank" style="color: var(--link);"> Mikey's </a>
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

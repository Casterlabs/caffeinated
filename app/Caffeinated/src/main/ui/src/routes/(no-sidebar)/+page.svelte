<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';

	import { onMount } from 'svelte';
	import { icon, iconColor } from '$lib/app.mjs';

	const koiUrl = st || window.svelte('Caffeinated', 'koiUrl');

	function resetKoi() {
		Caffeinated.koiUrl = 'wss://api.casterlabs.co/v2/koi';
		window.saucer.messages.emit(['app:restart']);
	}

	onMount(() => setTimeout(Caffeinated.UI.onUILoaded, 2000));
</script>

<div class="mt-10 flex flex-col items-center justify-center">
	<div class="w-64">
		<img
			src="/$caffeinated-sdk-root$/images/brand/wordmark/{$icon}/{$iconColor}.svg"
			class="h-auto w-auto"
			alt="Casterlabs Logo"
		/>
	</div>

	<div class="mt-8 mb-16 w-16">
		<LoadingSpinner />
	</div>

	{#if $koiUrl != 'wss://api.casterlabs.co/v2/koi'}
		<br />
		<!-- svelte-ignore a11y-missing-attribute -->
		<small>
			Having connection issues? Try <a class="text-link cursor-pointer" on:click={resetKoi}>
				switching back to normal Koi.
			</a>
		</small>
	{/if}
</div>

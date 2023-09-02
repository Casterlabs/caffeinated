<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';

	import { onMount } from 'svelte';
	import { icon, iconColor } from '$lib/app.mjs';

	const useBetaKoiPath = st || Caffeinated.svelte('useBetaKoiPath');

	function disableBetaKoi() {
		Caffeinated.useBetaKoiPath = false;
		Bridge.emit('app:restart');
	}

	onMount(() => setTimeout(Caffeinated.UI.onUILoaded, 1500));
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

	{#if $useBetaKoiPath}
		<br />
		<!-- svelte-ignore a11y-missing-attribute -->
		<small>
			Having connection issues? Try <a class="text-link cursor-pointer" on:click={disableBetaKoi}>
				disabling the Koi beta.
			</a>
		</small>
	{/if}
</div>

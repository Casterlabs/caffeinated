<script>
	import CSSIntermediate from '$lib/CSSIntermediate.svelte';

	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';
	import { onMount } from 'svelte';
	import hookIcons from '$lib/icons.mjs';
	import hookAnchors from '$lib/intercept-anchors.mjs';
	import appShim from '../components/appShim.mjs';
	import * as App from '$lib/app.mjs';
	import * as Currencies from '$lib/currencies.mjs';

	import { icon } from '$lib/app.mjs';

	onMount(() => {
		appShim();

		hookIcons('/$caffeinated-sdk-root$');
		hookAnchors(App.openLink);

		window.debug_goto = goto;
		window.debug_get = get;
		window.debug_App = App;
		window.debug_Currencies = Currencies;
	});
</script>

<svelte:head>
	{#if $icon == 'handdrawn'}
		<link rel="preconnect" href="https://fonts.googleapis.com" />
		<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
		<link
			href="https://fonts.googleapis.com/css2?family=Reenie+Beanie&display=swap"
			rel="stylesheet"
		/>
		<style id="silly-font-style">
			* {
				font-family: 'Reenie Beanie', cursive !important;
			}
		</style>
	{/if}
</svelte:head>

<CSSIntermediate>
	<slot />
</CSSIntermediate>

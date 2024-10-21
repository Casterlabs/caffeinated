<script>
	import CSSIntermediate from '$lib/layout/CSSIntermediate.svelte';

	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';
	import { onMount } from 'svelte';
	import hookIcons from '$lib/icons.mjs';
	import hookAnchors from '$lib/intercept-anchors.mjs';
	import * as appShim from '$lib/appShim.mjs';
	import * as App from '$lib/app.mjs';
	import * as Currencies from '$lib/currencies.mjs';

	import { icon, statusStates } from '$lib/app.mjs';
	import LocalizedText from '$lib/LocalizedText.svelte';

	export const STATUS_COLORS = {
		OPERATIONAL: ['green', 'white'],
		MAJOR_OUTAGE: ['red', 'white'],
		MINOR_OUTAGE: ['orange', 'white'],
		PARTIAL_OUTAGE: ['orange', 'white'],
		DEGRADED_PERFORMANCE: ['yellow', 'black'],
		MAINTENANCE: ['green', 'white']
	};

	let hideStatusBanner = false;

	onMount(() => {
		appShim.init();

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

{#await appShim.awaitPageLoad() then}
	<CSSIntermediate>
		<slot />
		{#if $statusStates?.length > 0 && !hideStatusBanner}
			{@const state = $statusStates[0]}

			<div
				class="absolute top-0 inset-x-0 py-1 m-1 rounded-md drop-shadow-lg"
				style:background={STATUS_COLORS[state.status][0]}
				style:color={STATUS_COLORS[state.status][1]}
			>
				<a
					class="block text-center underline"
					href={state.activeIncidents[0].link || 'https://status.casterlabs.co'}
					target="_blank"
				>
					<LocalizedText key="co.casterlabs.caffeinated.app.status.{state.status}"></LocalizedText>
				</a>

				<button
					class="absolute inset-y-0 right-1 flex items-center justify-center"
					on:click={() => (hideStatusBanner = true)}
				>
					<icon data-icon="icon/x-mark" />
				</button>
			</div>
		{/if}
	</CSSIntermediate>
{/await}

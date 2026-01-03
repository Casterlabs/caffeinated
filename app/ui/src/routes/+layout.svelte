<script lang="ts">
	import { goto } from '$app/navigation';
	import * as appShim from '$lib/appShim';
	import { storify } from '$lib/bridgeHelper';
	import { get } from 'svelte/store';

	import CSSIntermediate from '$lib/layout/CSSIntermediate.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconXMark } from '@casterlabs/heroicons-svelte';

	import { onMount } from 'svelte';

	export const STATUS_COLORS = {
		OPERATIONAL: ['green', 'white'],
		MAJOR_OUTAGE: ['red', 'white'],
		MINOR_OUTAGE: ['orange', 'white'],
		PARTIAL_OUTAGE: ['orange', 'white'],
		DEGRADED_PERFORMANCE: ['yellow', 'black'],
		MAINTENANCE: ['green', 'white']
	};

	interface StatusState {
		status: 'OPERATIONAL' | 'MAJOR_OUTAGE' | 'MINOR_OUTAGE' | 'PARTIAL_OUTAGE' | 'DEGRADED_PERFORMANCE' | 'MAINTENANCE';
		activeIncidents: { link: string }[];
	}

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
	const statusStates = storify(App, 'statusStates').readable<StatusState[]>();

	let hideStatusBanner = $state(false);

	$effect(() => {
		document.documentElement.style.fontSize = `${($uiPreferences?.zoom || 1) * 16}px`;
		document.documentElement.style.fontFamily = $uiPreferences?.uiFont || '';
	});

	onMount(() => {
		// @ts-ignore
		window.debug_goto = goto;
		// @ts-ignore
		window.debug_get = get;
		// @ts-ignore
		window.debug_App = App;
		// // @ts-ignore
		// window.debug_Currencies = Currencies;
	});
</script>

<svelte:head>
	{#if $uiPreferences?.icon == 'handdrawn'}
		<link rel="preconnect" href="https://fonts.googleapis.com" />
		<link rel="preconnect" href="https://fonts.gstatic.com" />
		<link href="https://fonts.googleapis.com/css2?family=Reenie+Beanie&display=swap" rel="stylesheet" />
		<style id="silly-font-style">
			* {
				font-family: 'Reenie Beanie', cursive !important;
			}
		</style>
	{/if}
</svelte:head>

{#await appShim.awaitPageLoad() then}
	<!-- svelte-ignore slot_element_deprecated -->
	<CSSIntermediate>
		<slot />

		{#if $statusStates && $statusStates.length && !hideStatusBanner}
			{@const state = $statusStates![0]}

			<div class="absolute top-0 inset-x-0 py-1 m-1 rounded-md drop-shadow-lg" style:background={STATUS_COLORS[state.status][0]} style:color={STATUS_COLORS[state.status][1]}>
				<a class="block text-center underline" href={state.activeIncidents[0].link || 'https://status.casterlabs.co'} target="_blank">
					<LocalizedText key="co.casterlabs.caffeinated.app.status.{state.status}"></LocalizedText>
				</a>

				<button class="absolute inset-y-0 right-1 flex items-center justify-center" onclick={() => (hideStatusBanner = true)}>
					<span class="sr-only">Dismiss</span>
					<IconXMark theme="solid" />
				</button>
			</div>
		{/if}
	</CSSIntermediate>
{/await}

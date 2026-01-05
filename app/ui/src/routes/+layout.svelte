<script lang="ts">
	import { goto } from '$app/navigation';
	import { STATUS_COLORS, appStatusStates, awaitPageLoad, isInApp } from '$lib/app-shim';
	import { storify } from '$lib/bridge-helper';
	import { glocale } from '$lib/locale/locale';
	import { get } from 'svelte/store';

	import CSSIntermediate from '$lib/layout/CSSIntermediate.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconXMark } from '@casterlabs/heroicons-svelte';

	import { onMount } from 'svelte';

	let isHanddrawn = $state(false);
	let hideStatusBanner = $state(false);

	onMount(() => {
		// @ts-ignore
		window.debug_goto = goto;
		// @ts-ignore
		window.debug_get = get;
		// @ts-ignore
		window.debug_App = App;
		// @ts-ignore
		window.debug_Glocale = glocale;
		// // @ts-ignore
		// window.debug_Currencies = Currencies;

		if (isInApp) {
			storify(AppConfig, 'uiPreferences')
				.readable<Awaited<typeof AppConfig.uiPreferences>>()
				.subscribe((prefs) => {
					isHanddrawn = prefs?.icon === 'handdrawn';
				});
		}
	});
</script>

<svelte:head>
	{#if isHanddrawn}
		<link rel="preconnect" href="https://fonts.googleapis.com" />
		<link rel="preconnect" href="https://fonts.gstatic.com" />
		<link href="https://fonts.googleapis.com/css2?family=Reenie+Beanie&display=swap" rel="stylesheet" />
		<style id="silly-font-style">
			* {
				font-family: 'Reenie Beanie', cursive !important;
				font-size-adjust: ex-height 0.75;
			}
		</style>
	{/if}
</svelte:head>

{#await awaitPageLoad() then}
	<!-- svelte-ignore slot_element_deprecated -->
	<CSSIntermediate>
		<slot />

		{#if $appStatusStates && $appStatusStates.length && !hideStatusBanner}
			{@const state = $appStatusStates![0]}

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

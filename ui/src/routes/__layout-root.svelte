<script>
	// Theme colors.
	import '$lib/css/colors/primary/gray.css';
	import '$lib/css/colors/primary/mauve.css';
	import '$lib/css/colors/primary/slate.css';
	import '$lib/css/colors/primary/sage.css';
	import '$lib/css/colors/primary/olive.css';
	import '$lib/css/colors/primary/sand.css';
	import '$lib/css/colors/primary/tomato.css';
	import '$lib/css/colors/primary/red.css';
	import '$lib/css/colors/primary/crimson.css';
	import '$lib/css/colors/primary/pink.css';
	import '$lib/css/colors/primary/plum.css';
	import '$lib/css/colors/primary/purple.css';
	import '$lib/css/colors/primary/violet.css';
	import '$lib/css/colors/primary/indigo.css';
	import '$lib/css/colors/primary/blue.css';
	import '$lib/css/colors/primary/cyan.css';
	import '$lib/css/colors/primary/teal.css';
	import '$lib/css/colors/primary/green.css';
	import '$lib/css/colors/primary/grass.css';
	import '$lib/css/colors/primary/orange.css';
	import '$lib/css/colors/primary/brown.css';
	import '$lib/css/colors/primary/sky.css';
	import '$lib/css/colors/primary/mint.css';
	import '$lib/css/colors/primary/lime.css';
	import '$lib/css/colors/primary/yellow.css';
	import '$lib/css/colors/primary/amber.css';
	import '$lib/css/colors/primary/gold.css';
	import '$lib/css/colors/primary/bronze.css';
	import '$lib/css/colors/base/gray.css';
	import '$lib/css/colors/base/mauve.css';
	import '$lib/css/colors/base/slate.css';
	import '$lib/css/colors/base/sage.css';
	import '$lib/css/colors/base/olive.css';
	import '$lib/css/colors/base/sand.css';
	import '$lib/css/colors/base/tomato.css';
	import '$lib/css/colors/base/red.css';
	import '$lib/css/colors/base/crimson.css';
	import '$lib/css/colors/base/pink.css';
	import '$lib/css/colors/base/plum.css';
	import '$lib/css/colors/base/purple.css';
	import '$lib/css/colors/base/violet.css';
	import '$lib/css/colors/base/indigo.css';
	import '$lib/css/colors/base/blue.css';
	import '$lib/css/colors/base/cyan.css';
	import '$lib/css/colors/base/teal.css';
	import '$lib/css/colors/base/green.css';
	import '$lib/css/colors/base/grass.css';
	import '$lib/css/colors/base/orange.css';
	import '$lib/css/colors/base/brown.css';
	import '$lib/css/colors/base/sky.css';
	import '$lib/css/colors/base/mint.css';
	import '$lib/css/colors/base/lime.css';
	import '$lib/css/colors/base/yellow.css';
	import '$lib/css/colors/base/amber.css';
	import '$lib/css/colors/base/gold.css';
	import '$lib/css/colors/base/bronze.css';
	import '$lib/css/colors/overlay-black.css';
	import '$lib/css/colors/overlay-white.css';

	// The actual gravy.
	import '$lib/css/app.css';
	import '$lib/css/colors/base.css';
	import '$lib/css/colors/primary.css';
	import '$lib/css/colors/misc.css';

	// Little helper to allow us to access the
	// stores but prevent SSR from erroring out.
	if (typeof global == 'undefined') {
		window.st = false;
	} else {
		global.st = {
			subscribe(callback) {
				callback(null);
				return () => {};
			},
			set() {} // No-OP
		};
	}

	import { dev } from '$app/env';
	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';
	import { onMount } from 'svelte';
	import * as App from '$lib/app.mjs';

	const baseColorTheme = st || Caffeinated.themeManager.svelte('baseColor');
	const primaryColorTheme = st || Caffeinated.themeManager.svelte('primaryColor');
	const effectiveAppearance = st || Caffeinated.themeManager.svelte('effectiveAppearance');
	const preferences = st || Caffeinated.UI.svelte('preferences');
	const hasCasterlabsPlus = st || Caffeinated.svelte('hasCasterlabsPlus');

	let hideDevButton = false;

	$: useLightTheme = $effectiveAppearance == 'LIGHT';
	$: effectiveAppearance,
		$effectiveAppearance &&
			console.info('[Layout]', 'Switching to (effective) theme:', $effectiveAppearance);

	// Set some app helpers.
	// Helps with making the UI more responsive.
	preferences.subscribe((prefs) => {
		if (!prefs) return;
		const { language, icon } = prefs;

		App.language.set(language);
		App.icon.set(icon);
	});

	hasCasterlabsPlus.subscribe((has) => {
		if (has === null) return;
		App.hasCasterlabsPlus.set(has);
	});

	$: useLightTheme, App.iconColor.set(useLightTheme ? 'black' : 'white');

	// Expose the goto() function to the Java side.
	onMount(() => {
		window.debug_goto = goto;
		window.debug_App = App;
		window.debug_get = get;
		Bridge.on('goto', ({ path }) => goto(path));
	});
</script>

<!--
	The app's theming is handled with data-theme-base, data-theme-primary, and class:dark-theme (we include data-theme-dark for debugging).
	All of the css files to make this happen are imported above.
-->

<div
	id="css-intermediate"
	class="w-full h-full bg-base-1 text-base-12"
	class:dark-theme={!useLightTheme}
	data-theme-dark={!useLightTheme}
	data-theme-base={$baseColorTheme || 'gray'}
	data-theme-primary={$primaryColorTheme || 'gray'}
>
	<slot />

	{#if dev}
		<!-- svelte-ignore missing-declaration -->
		<button
			class="fixed top-2 right-2 bg-gray-4 p-1.5 rounded-md"
			class:opacity-0={hideDevButton}
			title="Quick Theme Switch"
			on:click={async () => {
				window.Caffeinated.themeManager.setTheme(
					$baseColorTheme,
					$primaryColorTheme,
					useLightTheme ? 'DARK' : 'LIGHT'
				);
			}}
			on:contextmenu={(e) => {
				e.preventDefault();
				hideDevButton = !hideDevButton;
			}}
		>
			{#if useLightTheme}
				<icon data-icon="icon/sun" />
			{:else}
				<icon data-icon="icon/moon" />
			{/if}
		</button>
	{/if}
</div>

<style>
	#css-intermediate {
		--link: rgb(54, 100, 252);
		--error: rgb(224, 30, 30);
		--success: rgb(69, 204, 69);
	}

	#css-intermediate.dark-theme {
		--link: rgb(58, 137, 255);
		--error: rgb(252, 31, 31);
		--success: rgb(64, 187, 64);
	}
</style>

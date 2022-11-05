<script>
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
	import CSSIntermediate from '$lib/CSSIntermediate.svelte';
	import hookIcons from '$lib/icons.mjs';
	import hookAnchors from '$lib/intercept-anchors.mjs';
	import * as App from '$lib/app.mjs';

	const baseColorTheme = st || Caffeinated.themeManager.svelte('baseColor');
	const primaryColorTheme = st || Caffeinated.themeManager.svelte('primaryColor');
	const effectiveAppearance = st || Caffeinated.themeManager.svelte('effectiveAppearance');
	const preferences = st || Caffeinated.UI.svelte('preferences');

	let hideDevButton = false;

	// Set some app helpers.
	// Helps with making the UI more responsive.
	preferences.subscribe((prefs) => {
		if (!prefs) return;
		const { language, icon } = prefs;

		App.language.set(language);
		App.icon.set(icon);
	});

	baseColorTheme.subscribe(App.baseColor.set);
	primaryColorTheme.subscribe(App.primaryColor.set);
	effectiveAppearance.subscribe(App.appearance.set);

	// Expose the goto() function to the Java side.
	onMount(() => {
		hookIcons();
		hookAnchors(Caffeinated.UI.openLink);

		window.debug_goto = goto;
		window.debug_get = get;
		window.debug_App = App;
		Bridge.on('goto', ({ path }) => goto(path));
	});
</script>

<CSSIntermediate>
	<slot />

	{#if dev}
		{@const useLightTheme = $effectiveAppearance == 'LIGHT'}

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
</CSSIntermediate>

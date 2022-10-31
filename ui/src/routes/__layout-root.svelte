<script>
	import '$lib/css/app.css';
	import '$lib/css/base.css';
	import '$lib/css/primary.css';

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

	const effectiveTheme = st || Caffeinated.themeManager.svelte('effectiveTheme');
	const preferences = st || Caffeinated.UI.svelte('preferences');
	const hasCasterlabsPlus = st || Caffeinated.svelte('hasCasterlabsPlus');

	let hideDevButton = false;

	$: useLightTheme = $effectiveTheme?.appearance == 'LIGHT';
	$: effectiveTheme,
		$effectiveTheme && console.info('[Layout]', 'Switching to (effective) theme:', $effectiveTheme);

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

<svelte:head>
	{#if $effectiveTheme}
		{#if $effectiveTheme.appearance == 'DARK'}
			<link href="/src/lib/css/theme/_dark.css" rel="stylesheet" />
		{/if}

		{#if $effectiveTheme.id == 'CASTERLABS_LIGHT'}
			<link href="/src/lib/css/theme/mauve-crimson/light.css" rel="stylesheet" />
		{:else}
			<link href="/src/lib/css/theme/mauve-crimson/dark.css" rel="stylesheet" />
		{/if}
	{/if}
</svelte:head>

<div id="css-intermediate" class="w-full h-full bg-base-1 text-base-12 dark-theme">
	<slot />

	{#if dev}
		<!-- svelte-ignore missing-declaration -->
		<button
			class="fixed top-2 right-2 bg-gray-4 p-1.5 rounded-md"
			class:opacity-0={hideDevButton}
			title="Quick Theme Switch"
			on:click={async () => {
				Caffeinated.UI.updateAppearance({
					...(await Caffeinated.UI.preferences),
					theme: useLightTheme ? 'CASTERLABS_DARK' : 'CASTERLABS_LIGHT'
				});
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

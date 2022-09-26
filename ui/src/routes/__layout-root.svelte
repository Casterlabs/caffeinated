<script>
	import '$lib/css/app.css';

	import '$lib/css/colors/tomato.css';
	import '$lib/css/colors/red.css';
	import '$lib/css/colors/crimson.css';
	import '$lib/css/colors/pink.css';
	import '$lib/css/colors/plum.css';
	import '$lib/css/colors/purple.css';
	import '$lib/css/colors/violet.css';
	import '$lib/css/colors/indigo.css';
	import '$lib/css/colors/blue.css';
	import '$lib/css/colors/cyan.css';
	import '$lib/css/colors/teal.css';
	import '$lib/css/colors/green.css';
	import '$lib/css/colors/grass.css';
	import '$lib/css/colors/orange.css';
	import '$lib/css/colors/brown.css';
	import '$lib/css/colors/sky.css';
	import '$lib/css/colors/mint.css';
	import '$lib/css/colors/lime.css';
	import '$lib/css/colors/yellow.css';
	import '$lib/css/colors/amber.css';
	import '$lib/css/colors/gray.css';
	import '$lib/css/colors/mauve.css';
	import '$lib/css/colors/slate.css';
	import '$lib/css/colors/sage.css';
	import '$lib/css/colors/olive.css';
	import '$lib/css/colors/sand.css';
	import '$lib/css/colors/gold.css';
	import '$lib/css/colors/bronze.css';
	import '$lib/css/colors/overlay-black.css';
	import '$lib/css/colors/overlay-white.css';

	import { dev } from '$app/env';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';

	let hideDevButton = false;

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

	const effectiveTheme = st || Caffeinated.themeManager.svelte('effectiveTheme');
	$: useLightTheme = $effectiveTheme?.appearance == 'LIGHT';

	// Stupid ass workaround... Basically, we don't want to load the
	// dark css at all if we're in light mode and vice-versa.
	// So we need to dynamically import a component that has the css
	// we want. This could be solved if <style> elements resolved things
	// in $lib, but noooooooooooo.
	let themeComponent = null;

	$: effectiveTheme,
		$effectiveTheme && console.info('[Layout]', 'Switching to (effective) theme:', $effectiveTheme);

	$: useLightTheme,
		(useLightTheme
			? import('../components/layout/Theme_Light.svelte')
			: import('../components/layout/Theme_Dark.svelte')
		).then((c) => (themeComponent = c.default));

	onMount(() => {
		window.debug_goto = goto;
		Bridge.on('goto', ({ path }) => goto(path));
	});
</script>

<svelte:component this={themeComponent} />

<div
	id="css-intermediate"
	class="w-full h-full bg-mauve-1 text-mauve-12"
	class:dark-theme={!useLightTheme}
>
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
				<icon data-icon="sun" />
			{:else}
				<icon data-icon="moon" />
			{/if}
		</button>
	{/if}
</div>

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

	import { baseColor, primaryColor, appearance } from '$lib/app.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import { onMount } from 'svelte';

	let oldTimey = false;
	const console = createConsole('CSSIntermediate');

	let audio;
	let showClickPermission = true;

	onMount(() => {
		if (oldTimey) {
			audio = new Audio('/doots/the-entertainer.webm');
			audio.volume = 0.1;
			audio.loop = true;
			return () => audio.pause();
		}
	});

	$: useLightTheme = $appearance == 'LIGHT';
	$: appearance, $appearance && console.info('Switching to (effective) theme:', $appearance);
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
	data-theme-base={$baseColor}
	data-theme-primary={$primaryColor}
>
	<slot />

	<div id="context-menu" />

	{#if oldTimey}
		<img
			class="absolute inset-0 w-screen h-screen pointer-events-none"
			alt=""
			src="/doots/scratches.gif"
			style="mix-blend-mode: lighten;"
		/>
		<button
			class="absolute top-[50%] left-2 drop-shadow-md"
			on:click={() => {
				audio.pause();
				oldTimey = false;
			}}
		>
			<icon class="w-10 h-10" data-icon="icon/arrow-left-on-rectangle" />
		</button>
		<style>
			html {
				filter: sepia(1);
			}
		</style>
	{/if}

	{#if audio && showClickPermission}
		<div class="absolute inset-0 bg-black flex items-center justify-center">
			<button
				class="bg-base-2 border border-base-4 rounded-md text-xl px-2 py-1"
				on:click={() => {
					showClickPermission = false;
					audio.play();
				}}
			>
				Click here
			</button>
		</div>
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

<script lang="ts">
	import { themeBaseColor, themeEffectiveAppearance, themePrimaryColor } from '$lib/appShim';
	import createConsole from '$lib/console-helper';
	import '$lib/css/app.css';

	const console = createConsole('CSSIntermediate');

	let useLightTheme = $derived($themeEffectiveAppearance == 'LIGHT');
	$effect(() => {
		console.info('Switching to (effective) theme:', $themeEffectiveAppearance);
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
	data-theme-base={$themeBaseColor}
	data-theme-primary={$themePrimaryColor}
>
	<!-- svelte-ignore slot_element_deprecated -->
	<slot />

	<div id="context-menu"></div>
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

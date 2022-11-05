<script>
	// Theme colors.
	import './css/colors/primary/gray.css';
	import './css/colors/primary/mauve.css';
	import './css/colors/primary/slate.css';
	import './css/colors/primary/sage.css';
	import './css/colors/primary/olive.css';
	import './css/colors/primary/sand.css';
	import './css/colors/primary/tomato.css';
	import './css/colors/primary/red.css';
	import './css/colors/primary/crimson.css';
	import './css/colors/primary/pink.css';
	import './css/colors/primary/plum.css';
	import './css/colors/primary/purple.css';
	import './css/colors/primary/violet.css';
	import './css/colors/primary/indigo.css';
	import './css/colors/primary/blue.css';
	import './css/colors/primary/cyan.css';
	import './css/colors/primary/teal.css';
	import './css/colors/primary/green.css';
	import './css/colors/primary/grass.css';
	import './css/colors/primary/orange.css';
	import './css/colors/primary/brown.css';
	import './css/colors/primary/sky.css';
	import './css/colors/primary/mint.css';
	import './css/colors/primary/lime.css';
	import './css/colors/primary/yellow.css';
	import './css/colors/primary/amber.css';
	import './css/colors/primary/gold.css';
	import './css/colors/primary/bronze.css';
	import './css/colors/base/gray.css';
	import './css/colors/base/mauve.css';
	import './css/colors/base/slate.css';
	import './css/colors/base/sage.css';
	import './css/colors/base/olive.css';
	import './css/colors/base/sand.css';
	import './css/colors/base/tomato.css';
	import './css/colors/base/red.css';
	import './css/colors/base/crimson.css';
	import './css/colors/base/pink.css';
	import './css/colors/base/plum.css';
	import './css/colors/base/purple.css';
	import './css/colors/base/violet.css';
	import './css/colors/base/indigo.css';
	import './css/colors/base/blue.css';
	import './css/colors/base/cyan.css';
	import './css/colors/base/teal.css';
	import './css/colors/base/green.css';
	import './css/colors/base/grass.css';
	import './css/colors/base/orange.css';
	import './css/colors/base/brown.css';
	import './css/colors/base/sky.css';
	import './css/colors/base/mint.css';
	import './css/colors/base/lime.css';
	import './css/colors/base/yellow.css';
	import './css/colors/base/amber.css';
	import './css/colors/base/gold.css';
	import './css/colors/base/bronze.css';
	import './css/colors/overlay-black.css';
	import './css/colors/overlay-white.css';

	// The actual gravy.
	import './css/app.css';
	import './css/colors/base.css';
	import './css/colors/primary.css';
	import './css/colors/misc.css';

	import { baseColor, primaryColor, appearance } from './app.mjs';
	import createConsole from './console-helper.mjs';

	const console = createConsole('CSSIntermediate');

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

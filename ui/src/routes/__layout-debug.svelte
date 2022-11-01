<script>
	// Theme colors.
	import '$lib/css/colors/base/mauve.css';
	import '$lib/css/colors/primary/crimson.css';

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
</script>

<!--
	The app's theming is handled with data-theme-base, data-theme-primary, and class:dark-theme (we include data-theme-dark for debugging).
	All of the css files to make this happen are imported above.
-->

<div
	id="css-intermediate"
	class="w-full h-full bg-base-1 text-base-12"
	class:dark-theme={true}
	data-theme-dark={true}
	data-theme-base="mauve"
	data-theme-primary="crimson"
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

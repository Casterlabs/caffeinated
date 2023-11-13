<script>
	import createConsole from '$lib/console-helper.mjs';
	import { t } from '$lib/app.mjs';

	const console = createConsole('LocalizedProperty');

	export let opts = {};
	export let key;
	export let property = 'title';

	export let prefix = 'co.casterlabs.caffeinated.app.';

	let self;

	async function render() {
		const result = await t(prefix + key, opts, slotMapping);
		console.debug('Localized:', key, result);
		self.parentElement.setAttribute(property, result);
	}

	// Rerender on change
	$: key, render();
	$: opts, render();
</script>

<span bind:this={self} data-dummy="LocalizedProperty" />

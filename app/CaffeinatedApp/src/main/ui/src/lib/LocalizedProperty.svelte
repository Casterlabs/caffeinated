<script>
	import createConsole from '$lib/console-helper.mjs';
	import { t } from '$lib/app.mjs';

	const console = createConsole('LocalizedProperty');

	export let key;
	export let opts = {};
	export let property = '';

	let contentHash;
	let self;

	async function render() {
		const hash = JSON.stringify({ key, opts, property });
		if (hash == contentHash) {
			// Avoid re-render.
			return;
		}

		const result = await t(key, opts, []);
		console.debug('Localized:', key, result);
		self.parentElement.setAttribute(property, result);
	}

	// Rerender on change
	$: key, render();
	$: opts, render();
</script>

<span bind:this={self} data-dummy="LocalizedProperty" style="display: none;" />

<script>
	import { createEventDispatcher, onMount } from 'svelte';
	import { appearance } from '$lib/app.mjs';

	const dispatch = createEventDispatcher();

	export let value = '';
	export let language = '';

	export let typescriptTypings = null;

	let container;

	onMount(() => {
		if (typescriptTypings != null) {
			const compilerOptions = monaco.languages.typescript.javascriptDefaults.getCompilerOptions();
			monaco.languages.typescript.javascriptDefaults.setCompilerOptions({
				...compilerOptions,
				target: monaco.languages.typescript.ScriptTarget.ES5,
				allowNonTsExtensions: true,
				lib: ['es7', 'es2022']
			});

			monaco.languages.typescript.javascriptDefaults.addExtraLib(
				typescriptTypings,
				'file:///lib.d.ts'
			);
		}

		const editor = monaco.editor.create(container, {
			value,
			language,
			automaticLayout: true,
			lineNumbersMinChars: 2,
			minimap: { enabled: false },
			theme: $appearance == 'DARK' ? 'vs-dark' : 'vs-light'
		});

		// Ugly check loop. Thanks Monaco!
		const checkInterval = setInterval(() => {
			const currValue = editor.getValue();

			if (currValue != value) {
				value = currValue;
				dispatch('value', value);
			}
		}, 500);

		return () => clearInterval(checkInterval);
	});
</script>

<div bind:this={container} class="h-full w-full" />

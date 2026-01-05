<script lang="ts">
	import { themeEffectiveAppearance } from '$lib/app-shim';

	import { onMount } from 'svelte';

	interface Props {
		value: string;
		language: string;
		typescriptTypings?: string | null;
		onchange?: (value: string) => void;
	}

	let { value = $bindable(''), language, typescriptTypings = null, onchange }: Props = $props();

	let container: HTMLDivElement;

	onMount(() => {
		// @ts-ignore
		const monaco = window.monaco as any;

		if (typescriptTypings != null) {
			const compilerOptions = monaco.languages.typescript.javascriptDefaults.getCompilerOptions();
			monaco.languages.typescript.javascriptDefaults.setCompilerOptions({
				...compilerOptions,
				target: monaco.languages.typescript.ScriptTarget.ES5,
				allowNonTsExtensions: true,
				lib: ['es7', 'es2022']
			});

			monaco.languages.typescript.javascriptDefaults.addExtraLib(typescriptTypings, 'file:///lib.d.ts');
		}

		const editor = monaco.editor.create(container, {
			value,
			language,
			automaticLayout: true,
			lineNumbersMinChars: 2,
			minimap: { enabled: false },
			theme: $themeEffectiveAppearance == 'DARK' ? 'vs-dark' : 'vs-light'
		});

		// Ugly check loop. Thanks Monaco!
		const id = setInterval(() => {
			const currValue = editor.getValue();

			if (currValue != value) {
				value = currValue;
				onchange?.(value);
			}
		}, 500);
		return () => clearInterval(id);
	});
</script>

<div bind:this={container} class="h-full w-full"></div>

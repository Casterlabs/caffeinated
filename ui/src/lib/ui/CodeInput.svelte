<script>
	import { createEventDispatcher, onMount } from 'svelte';
	import { appearance } from '$lib/app.mjs';

	const dispatch = createEventDispatcher();

	export let value = '';
	export let language = '';

	let container;

	onMount(() => {
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

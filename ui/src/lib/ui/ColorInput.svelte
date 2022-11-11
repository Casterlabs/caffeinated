<script>
	import { createEventDispatcher } from 'svelte';
	import Debouncer from '$lib/debouncer.mjs';

	const dispatch = createEventDispatcher();
	const debouncer = new Debouncer();

	export let value = '#000000';

	function db() {
		debouncer.debounce(() => {
			dispatch('value', value);
		});
	}
</script>

<input
	class="px-2 py-1 cursor-pointer text-base-12 rounded-md border transition hover:border-base-8 border-base-7 bg-base-1 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
	type="color"
	bind:value
	on:input={db}
/>

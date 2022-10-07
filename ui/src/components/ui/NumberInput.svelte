<script>
	import { createEventDispatcher } from 'svelte';
	import { t } from '$lib/translate.mjs';
	import Debouncer from '$lib/debouncer.mjs';

	const dispatch = createEventDispatcher();
	const debouncer = new Debouncer();

	export let value = 0;
	export let min = Number.MIN_SAFE_INTEGER;
	export let max = Number.MAX_SAFE_INTEGER;

	function db() {
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}

		debouncer.debounce(() => {
			dispatch('value', value);
		});
	}
</script>

<input
	class="px-2 py-1 text-mauve-12 rounded-md border transition hover:border-mauve-8 border-mauve-7 bg-mauve-1 shadow-sm focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7 text-sm"
	type="number"
	{min}
	{max}
	bind:value
	on:mousewheel={(e) => {
		value -= e.deltaY / 100;
		db();
	}}
	on:input={db}
/>

<script>
	import { createEventDispatcher } from 'svelte';
	import { t } from '$lib/app.mjs';
	import Debouncer from '$lib/debouncer.mjs';

	const dispatch = createEventDispatcher();
	const debouncer = new Debouncer(100 /* ~10hz */);

	export let value = 0;
	export let min = Number.MIN_SAFE_INTEGER;
	export let max = Number.MAX_SAFE_INTEGER;
	export let disabled = false;

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
	class="px-2 py-1 w-full text-base-12 rounded-md border transition hover:border-base-8 border-base-7 bg-base-1 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
	class:opacity-60={disabled}
	type="number"
	{min}
	{max}
	{disabled}
	bind:value
	on:mousewheel={(e) => {
		if (disabled) return;
		value -= e.deltaY / 100;
		db();
	}}
	on:input={db}
/>

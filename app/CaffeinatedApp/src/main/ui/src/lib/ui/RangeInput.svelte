<script>
	import { createEventDispatcher } from 'svelte';
	import { t } from '$lib/app.mjs';
	import Debouncer from '$lib/debouncer.mjs';

	const dispatch = createEventDispatcher();
	const debouncer = new Debouncer(100 /* ~10hz */);

	export let value = 0;
	export let step = 0.1;
	export let min = 0;
	export let max = 1;
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
	class="range"
	type="range"
	bind:value
	on:mousewheel={(e) => {
		if (disabled) return;
		if (e.deltaY < 0) {
			value -= step;
		} else {
			value += step;
		}
		db();
	}}
	on:input={db}
	{step}
	{min}
	{max}
/>

<style>
	.range {
		appearance: none;
		height: 4px !important;
		transform: translateY(-3px);
		background-color: var(--base6);
	}

	.range::-webkit-slider-runnable-track {
		width: 300px;
		height: 2px;
		border: none;
		border-radius: 3px;
	}

	.range::-webkit-slider-thumb {
		-webkit-appearance: none;
		border: none;
		height: 12px;
		width: 12px;
		border-radius: 500%;
		background: var(--base1);
		border: 2px solid var(--primary10);
		margin-top: -5px;
		cursor: pointer;
	}
</style>

<script>
	import { createEventDispatcher } from 'svelte';
	import { t } from '$lib/app.mjs';
	import { writable } from 'svelte/store';
	import Debouncer from '$lib/debouncer.mjs';

	const dispatch = createEventDispatcher();
	const debouncer = new Debouncer();

	export let value = '';
	export let placeholder = '';
	export let autofocus = false;

	let placeholderT = writable('');
	$: placeholder, t(placeholder).then(placeholderT.set);
</script>

<!-- svelte-ignore a11y-autofocus -->
<input
	type="password"
	class="px-1.5 py-1 inline-block w-full text-base-12 rounded-md border transition hover:border-base-8 border-base-7 bg-base-1 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
	placeholder={$placeholderT}
	{autofocus}
	bind:value
	on:input={() => {
		debouncer.debounce(() => {
			dispatch('value', value);
		});
	}}
/>

<style>
	input::placeholder {
		color: var(--base8);
	}
</style>

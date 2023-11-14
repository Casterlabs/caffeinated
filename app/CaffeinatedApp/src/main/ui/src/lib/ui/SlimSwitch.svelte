<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';

	import { createEventDispatcher } from 'svelte';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let title = '';
	export let checked = false;

	function toggle() {
		checked = !checked;
		dispatch('value', checked);
	}
</script>

<span class="sr-only" id="{ID}-label">
	<LocalizedText key={title} />
</span>

<button
	type="button"
	role="switch"
	class="relative ml-4 inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary-7 focus:ring-offset-2 ring-offset-current text-base-2"
	class:bg-base-9={!checked}
	class:bg-primary-9={checked}
	aria-checked={checked}
	aria-labelledby="{ID}-label"
	on:click={toggle}
>
	<LocalizedProperty key={title} property="title" />
	<span
		aria-hidden="true"
		class="inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
		class:translate-x-0={!checked}
		class:translate-x-5={checked}
	/>
</button>

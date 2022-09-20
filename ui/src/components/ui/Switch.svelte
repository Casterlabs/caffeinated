<script>
	import LocalizedText from '../LocalizedText.svelte';
	import FocusListener from '../interaction/FocusListener.svelte';

	import { createEventDispatcher } from 'svelte';
	import { fade } from 'svelte/transition';

	const ID = Math.random().toString(36);

	const dispatch = createEventDispatcher();

	export let title = 'My switch button';
	export let description = 'Click to switch!';
	export let checked = false;

	function toggle() {
		checked = !checked;
		dispatch('value', checked);
	}
</script>

<div class="flex flex-col">
	<p class="text-sm font-medium text-mauve-12" id="{ID}-label">
		<LocalizedText key={title} />
	</p>
	<p class="text-sm text-mauve-11" id="{ID}-description">
		<LocalizedText key={description} />
	</p>
</div>

<button
	type="button"
	role="switch"
	class="relative ml-4 inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-crimson-7 focus:ring-offset-2 ring-offset-current text-mauve-2"
	class:bg-mauve-9={!checked}
	class:bg-crimson-9={checked}
	aria-checked="true"
	aria-labelledby="{ID}-label"
	aria-describedby="{ID}-description"
	on:click={toggle}
>
	<span
		aria-hidden="true"
		class="inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
		class:translate-x-0={!checked}
		class:translate-x-5={checked}
	/>
</button>

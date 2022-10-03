<script>
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();
</script>

<button
	type="button"
	class="relative w-fit h-fit cursor-pointer rounded-full p-1.5 transition bg-mauve-3 border border-mauve-6 hover:bg-mauve-5 hover:border-mauve-8 focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7 text-mauve-12 hover:text-mauve-11 text-center text-sm"
	on:click={() => dispatch('click')}
>
	<slot />
</button>

<script>
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();
</script>

<button
	type="button"
	class="relative w-fit cursor-pointer rounded-md py-1.5 px-2 transition-[background-color] bg-mauve-3 border border-mauve-6 hover:bg-mauve-5 hover:border-mauve-8 focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7 text-center text-xs"
	on:click={() => dispatch('click')}
>
	<slot />
</button>

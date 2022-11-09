<script>
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	export let title;
</script>

<button
	{title}
	type="button"
	class="relative w-fit h-fit cursor-pointer rounded-full p-1.5 transition bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-base-12 hover:text-base-11 text-center text-sm"
	on:click={() => dispatch('click')}
>
	<slot />
</button>

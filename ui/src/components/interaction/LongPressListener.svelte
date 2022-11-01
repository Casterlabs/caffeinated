<script>
	import { onMount, createEventDispatcher } from 'svelte';

	const DELAY = 1200;

	const dispatch = createEventDispatcher();
	let element;

	onMount(() => {
		element.addEventListener('long-press', (e) => {
			e.preventDefault();
			dispatch('long-press', e.detail);
		});
	});
</script>

<div bind:this={element} data-long-press-delay={DELAY}>
	<slot />
</div>

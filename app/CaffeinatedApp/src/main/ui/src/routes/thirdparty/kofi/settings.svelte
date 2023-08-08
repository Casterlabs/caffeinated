<script>
	import { onMount } from 'svelte';

	let url = '';

	onMount(() => {
		Widget.on('url', (u) => (url = u));
		console.log(Widget);
	});
</script>

{url}

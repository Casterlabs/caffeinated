<script>
	import '$lib/css/app.css';

	import hookIcons from '$lib/icons.mjs';
	import { onMount } from 'svelte';

	onMount(() => {
		hookIcons('/$caffeinated-sdk-root$');
	});
</script>

<slot />

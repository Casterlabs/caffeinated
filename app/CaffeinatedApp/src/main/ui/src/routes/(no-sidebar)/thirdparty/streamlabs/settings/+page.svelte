<script>
	import SlimButton from '$lib/ui/SlimButton.svelte';

	import { onMount } from 'svelte';

	let hasToken = false;

	onMount(() => {
		Widget.on('hasToken', (has) => (hasToken = has));
		Widget.on('init', () => {
			Widget.emit('hasToken');
		});
		console.log(Widget);
	});
</script>

{#if hasToken}
	<SlimButton on:click={() => Widget.emit('signout')}>Signout</SlimButton>
	<SlimButton on:click={() => Widget.emit('test')}>Test</SlimButton>
{:else}
	<SlimButton on:click={() => Widget.emit('signin')}>Signin</SlimButton>
{/if}

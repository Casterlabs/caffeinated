<script>
	import { onMount } from 'svelte';
	import OAuthSignin from '$lib/OAuthSignin.svelte';
	import parseQueryParameters from '$lib/parseQueryParameters.mjs';

	let type;
	let platform;

	onMount(() => {
		const params = parseQueryParameters();
		type = params.type;
		platform = params.platform;
	});
</script>

{#if type && platform}
	<OAuthSignin {type} {platform} />
{/if}

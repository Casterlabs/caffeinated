<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { onMount } from 'svelte';

	export let platform;
	export let isKoi = false;

	onMount(() => {
		Caffeinated.auth.requestOAuthSignin(platform, isKoi, true);
	});

	function cancelAuth() {
		Caffeinated.auth.cancelSignin();
		history.back();
	}
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div class="mt-10 flex flex-col items-center justify-center">
	<div class="mt-8 mb-16 w-16">
		<LoadingSpinner />
	</div>

	<a on:click={cancelAuth} class="text-nqp cursor-pointer">
		<LocalizedText key="navigation.want_to_go_back" />
	</a>
</div>

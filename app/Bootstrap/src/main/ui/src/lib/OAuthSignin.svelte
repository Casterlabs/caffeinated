<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { onMount } from 'svelte';

	export let type;
	export let platform;

	onMount(() => {
		const dontGoBack =
			location.search.includes('?dontGoBack') || location.search.includes('&dontGoBack');
		Caffeinated.auth.requestOAuthSignin(type, platform, !dontGoBack, null);
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

	<!-- svelte-ignore a11y-click-events-have-key-events -->
	<!-- svelte-ignore a11y-no-static-element-interactions -->
	<a on:click={cancelAuth} class="text-nqp cursor-pointer">
		<LocalizedText key="co.casterlabs.caffeinated.app.ui.navigation.want_to_go_back" />
	</a>
</div>

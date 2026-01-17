<script lang="ts">
	import LoadingSpinner from '$lib/layout/LoadingSpinner.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';

	import { onMount } from 'svelte';

	onMount(() => {
		const searchParams = new URLSearchParams(location.search);
		const dontGoBack = !!searchParams.get('dontGoBack');
		const type = searchParams.get('type') || 'koi';
		const platform = searchParams.get('platform')!;

		AppAuth.requestOAuthSignin(type, platform.toLowerCase(), !dontGoBack, null);
	});

	function cancelAuth() {
		AppAuth.cancelSignin();
		history.back();
	}
</script>

<div class="mt-10 flex flex-col items-center justify-center">
	<div class="mt-8 mb-16 w-16">
		<LoadingSpinner />
	</div>

	<button onclick={cancelAuth} class="text-primary-11 cursor-pointer">
		<LocalizedText key="co.casterlabs.caffeinated.app.ui.navigation.want_to_go_back" />
	</button>
</div>

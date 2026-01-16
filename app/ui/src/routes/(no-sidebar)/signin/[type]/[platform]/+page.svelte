<script lang="ts">
	import type { PageData } from './$types';

	import LoadingSpinner from '$lib/layout/LoadingSpinner.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';

	import { onMount } from 'svelte';

	let { data }: { data: PageData } = $props();

	onMount(() => {
		const dontGoBack = location.search.includes('?dontGoBack') || location.search.includes('&dontGoBack');
		AppAuth.requestOAuthSignin(data.type, data.platform.toLowerCase(), !dontGoBack, null);
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

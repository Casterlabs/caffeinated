<script>
	import { t } from '$lib/translate.mjs';
	import { language } from '$lib/app.mjs';
	import { onDestroy } from 'svelte';

	export let title;

	onDestroy(() => {
		document.title = 'Casterlabs-Caffeinated';
	});
</script>

<svelte:head>
	<title>
		Casterlabs-Caffeinated - {t($language, title)}
	</title>
</svelte:head>

<script>
	import { t } from '$lib/app.mjs';
	import { onDestroy } from 'svelte';

	const BASE = 'Casterlabs-Caffeinated';

	/** @type {string[]|string} */
	export let title;
	let titleString = '';

	async function recalculateTitle() {
		titleString = BASE;
		let parts = title;

		if (typeof title == 'string') {
			parts = [title]; // Create an array.
		}

		for (const part of parts) {
			titleString += ' - ';
			titleString += await t(part);
		}

		if (titleString.includes('LOCALE_PRERENDER:')) {
			titleString = BASE;
			setTimeout(recalculateTitle, 15);
		}
	}

	$: title, recalculateTitle();

	onDestroy(() => {
		if (typeof document != 'undefined') {
			document.title = BASE;
		}
	});
</script>

<svelte:head>
	<title>
		{titleString}
	</title>
</svelte:head>

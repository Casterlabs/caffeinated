<script>
	import CSSIntermediate from '$lib/CSSIntermediate.svelte';

	import { goto } from '$app/navigation';
	import { get } from 'svelte/store';
	import { onMount } from 'svelte';
	import hookIcons from '$lib/icons.mjs';
	import hookAnchors from '$lib/intercept-anchors.mjs';
	import appShim from '../components/appShim.mjs';
	import * as App from '$lib/app.mjs';
	import * as Currencies from '$lib/currencies.mjs';

	onMount(() => {
		appShim();

		hookIcons('/$caffeinated-sdk-root$');
		hookAnchors(App.openLink);

		window.debug_goto = goto;
		window.debug_get = get;
		window.debug_App = App;
		window.debug_Currencies = Currencies;
	});
</script>

<CSSIntermediate>
	<slot />
</CSSIntermediate>

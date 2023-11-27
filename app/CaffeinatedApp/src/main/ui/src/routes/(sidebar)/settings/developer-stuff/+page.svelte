<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';

	import { goto } from '$app/navigation';
	import {
		ALL_STREAMING_SERVICES,
		SPECIAL_SIGNIN,
		PORTAL_SIGNIN,
		openAuthPortal
	} from '$lib/caffeinatedAuth.mjs';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';

	let manualAuthPlatform = 0;
</script>

<SelectMenu
	title="Manually authenticate platform"
	options={ALL_STREAMING_SERVICES}
	bind:value={manualAuthPlatform}
	on:value={({ detail: index }) => {
		if (index == 0) return;
		manualAuthPlatform = 0;

		const platform = ALL_STREAMING_SERVICES[index];
		if (SPECIAL_SIGNIN[platform]) {
			goto(`${SPECIAL_SIGNIN[platform]}?platform=${platform.toLowerCase()}`);
		} else if (PORTAL_SIGNIN.includes(platform)) {
			openAuthPortal(platform, false);
			return;
		} else {
			goto(`/$caffeinated-sdk-root$/signin/oauth?type=koi&platform=${platform.toLowerCase()}`);
		}
	}}
/>

<br />
<br />

<SlimTextArea
	rows="1"
	resize={false}
	placeholder="Navigate to path"
	on:value={({ detail }) => {
		goto('/$caffeinated-sdk-root$' + detail);
	}}
/>

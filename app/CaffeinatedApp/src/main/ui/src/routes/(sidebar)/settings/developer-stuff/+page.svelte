<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';

	import { goto } from '$app/navigation';
	import {
		ALL_STREAMING_SERVICES,
		SPECIAL_SIGNIN,
		PORTAL_SIGNIN,
		openAuthPortal
	} from '$lib/caffeinatedAuth.mjs';

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

<a
	class="text-link"
	href="/$caffeinated-sdk-root$/settings/applet?id=co.casterlabs.thirdparty.streamlabs.settings.settings_applet"
>
	Streamlabs Integration
</a>

<a
	class="text-link"
	href="/$caffeinated-sdk-root$/settings/applet?id=co.casterlabs.thirdparty.kofi.settings.settings_applet"
>
	Ko-fi Integration
</a>

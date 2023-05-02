<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';

	import { goto } from '$app/navigation';
	import { ALL_STREAMING_SERVICES, SPECIAL_SIGNIN } from '../../components/caffeinatedAuth.mjs';

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
			goto('/$caffeinated-sdk-root$' + SPECIAL_SIGNIN[platform]);
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

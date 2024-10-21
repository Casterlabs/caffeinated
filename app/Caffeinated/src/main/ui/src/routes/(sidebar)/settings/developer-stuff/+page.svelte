<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';
	import SlimSearchMenu from '$lib/ui/SlimSearchMenu.svelte';

	import { goto } from '$app/navigation';
	import {
		ALL_STREAMING_SERVICES,
		SPECIAL_SIGNIN,
		PORTAL_SIGNIN,
		openAuthPortal
	} from '$lib/caffeinatedAuth.mjs';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';

	const uiPreferences = st || window.svelte('Caffeinated.UI', 'preferences');
	let manualAuthPlatform = 0;

	$: uiPreferences, $uiPreferences && console.debug('UI Preferences:', $uiPreferences);

	async function setUIPreferenceItem(name, value) {
		Caffeinated.UI.updateAppearance({
			...(await Caffeinated.UI.preferences),
			[name]: value
		});
	}
</script>

<SelectMenu
	title="Manually authenticate platform"
	options={['Select a platform', ...ALL_STREAMING_SERVICES]}
	bind:value={manualAuthPlatform}
	on:value={({ detail: index }) => {
		if (index == 0) return;
		manualAuthPlatform = 0;

		const platform = ALL_STREAMING_SERVICES[index - 1];
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

<br />
<br />

Override UI Font: <TextArea
	rows="1"
	resize={false}
	value={$uiPreferences?.uiFont}
	on:value={({ detail: value }) => setUIPreferenceItem('uiFont', value)}
/>

<script lang="ts">
	import { goto } from '$app/navigation';
	import { modify, storify } from '$lib/bridge-helper';

	import { Input, Select } from '@casterlabs/ui';

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
</script>

<label>
	Manually authenticate platform:
	<br />
	<Select
		onchange={(e) => {
			const value = (e.target as HTMLSelectElement).value;
			AppAuth.requestOAuthSignin('koi', value.toLowerCase(), false, Math.random().toString(36).substring(2, 15));
		}}
	>
		<option selected disabled> Select a platform... </option>

		{#await AppAuth.ALL then ALL_PLATFORMS}
			{#each ALL_PLATFORMS as platform}
				<option value={platform}>
					{platform}
				</option>
			{/each}
		{/await}
	</Select>
</label>

<br />
<br />

<Input
	type="text"
	placeholder="Navigate to path"
	onchange={(e) => {
		const value = (e.target as HTMLInputElement).value;
		goto('/$caffeinated-sdk-root$' + value);
	}}
/>

<br />
<br />

Override UI Font: <Input
	type="text"
	value={$uiPreferences?.uiFont}
	onchange={(e) => {
		const value = (e.target as HTMLInputElement).value;
		modify(AppConfig, 'uiPreferences', 'uiFont', value);
	}}
/>

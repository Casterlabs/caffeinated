<script lang="ts">
	import { getCurrencies } from '$lib/currencies';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { Select } from '@casterlabs/ui';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	let { widget, settingsKey, settingsItem }: Props = $props();

	let selected = $state(widget.settings[settingsKey] as string);

	const options = (async () => {
		switch (settingsItem.type) {
			case 'DROPDOWN':
				return Object.entries(settingsItem.extraData.options) as [string, string][];

			case 'CURRENCY':
				return (await getCurrencies()).currencies //
					.map((currency) => [currency.currencyCode, `${currency.currencyCode} - ${currency.currencyName}`]) as [string, string][];

			case 'FONT':
				return await AppUI.fonts;

			default:
				return []; // Shut up ts
		}
	})();
</script>

<Select
	class="w-full"
	bind:value={selected}
	onchange={() => {
		AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, selected);
	}}
>
	{#await options then opts}
		{#each opts as option}
			{#if Array.isArray(option)}
				{@const [optionValue, optionLabel] = option}
				<option value={optionValue}>
					<LocalizedText key={optionLabel} />
				</option>
			{:else}
				<option value={option}>
					{option}
				</option>
			{/if}
		{/each}
	{/await}
</Select>

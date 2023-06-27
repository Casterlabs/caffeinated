<script>
	import LocalizedText from '../LocalizedText.svelte';
	import NumberInput from './NumberInput.svelte';
	import Switch from './Switch.svelte';
	import ColorInput from './ColorInput.svelte';
	import SlimPassword from './SlimPassword.svelte';
	import SlimTextArea from './SlimTextArea.svelte';
	import SlimSelectMenu from './SlimSelectMenu.svelte';
	import RangeInput from './RangeInput.svelte';
	import FileInput from './FileInput.svelte';
	import SlimSearchMenu from './SlimSearchMenu.svelte';

	import { fonts, currencies } from '$lib/misc.mjs';

	export let widget;
	export let widgetSettingsSection;
	export let widgetSettingsItem;

	const type = widgetSettingsItem.type.toLowerCase();
	const settingsKey = `${widgetSettingsSection.id}.${widgetSettingsItem.id}`;

	let value = widget.settings[settingsKey];

	if (value == undefined || value == null) {
		value = widgetSettingsItem.extraData.defaultValue;
	}

	function onInput() {
		Caffeinated.plugins.editWidgetSettingsItem(widget.id, settingsKey, value);
	}
</script>

{#if type == 'html'}
	{@html widgetSettingsItem.extraData.html}
{:else if type == 'checkbox'}
	<Switch title={widgetSettingsItem.name} description="" bind:checked={value} on:value={onInput} />
{:else}
	<div class="flex items-center justify-between w-full">
		<div class="flex flex-col">
			<p class="text-sm font-medium text-base-12">
				<LocalizedText key={widgetSettingsItem.name} />
			</p>
		</div>

		<div class="text-right w-40">
			{#if type == 'color'}
				<ColorInput bind:value on:value={onInput} />
			{:else if type == 'number'}
				{@const { step, min, max } = widgetSettingsItem.extraData}
				<NumberInput {step} {min} {max} bind:value on:value={onInput} />
			{:else if type == 'text'}
				<SlimTextArea rows="1" resize={false} bind:value on:value={onInput} />
			{:else if type == 'textarea'}
				<SlimTextArea rows="1" resize={true} bind:value on:value={onInput} />
			{:else if type == 'dropdown'}
				{@const { options } = widgetSettingsItem.extraData}
				<SlimSelectMenu
					title={widgetSettingsItem.name}
					width="full"
					bind:value
					on:value={onInput}
					{options}
				/>
			{:else if type == 'password'}
				<SlimPassword bind:value on:value={onInput} />
			{:else if type == 'currency'}
				<SlimSelectMenu
					title={widgetSettingsItem.name}
					width="full"
					bind:value
					on:value={onInput}
					options={$currencies.currencies.reduce(
						(arr, currency) => ({ ...arr, [currency.currencyCode]: currency.currencyName }),
						{}
					)}
				/>
			{:else if type == 'font'}
				<SlimSearchMenu
					title={widgetSettingsItem.name}
					width="full"
					bind:value
					on:value={onInput}
					search={(query) => {
						query = query.toLowerCase();
						let result = [];

						for (const font of $fonts) {
							if (font.toLowerCase().includes(query)) {
								result.push(font);
							}

							if (result.length == 6) {
								break; // Max of 6 results.
							}
						}

						return result;
					}}
				/>
			{:else if type == 'range'}
				{@const { step, min, max } = widgetSettingsItem.extraData}
				<RangeInput {step} {min} {max} bind:value on:value={onInput} />
			{:else if type == 'file'}
				{@const { allowed } = widgetSettingsItem.extraData}
				<FileInput bind:value allowedTypes={allowed} on:value={onInput} />
			{:else}
				... {type}
			{/if}
		</div>
	</div>
{/if}

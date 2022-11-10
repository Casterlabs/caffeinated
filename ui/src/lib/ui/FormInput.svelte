<script>
	import LocalizedText from '../LocalizedText.svelte';
	import NumberInput from './NumberInput.svelte';
	import Switch from './Switch.svelte';
	import ColorInput from './ColorInput.svelte';

	import Debouncer from '$lib/debouncer.mjs';

	const debouncer = new Debouncer();

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
		debouncer.debounce(() => {
			Caffeinated.plugins.editWidgetSettingsItem(widget.id, settingsKey, value);
		});
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

		{#if type == 'color'}
			<ColorInput bind:value on:value={onInput} />
		{:else if type == 'number'}
			{@const { step, min, max } = widgetSettingsItem.extraData}
			<NumberInput {step} {min} {max} bind:value on:value={onInput} />
		{:else if type == 'dropdown'}
			//dropdown
		{:else if type == 'text'}
			//text
		{:else if type == 'textarea'}
			//textarea
		{:else if type == 'password'}
			//password
		{:else if type == 'currency'}
			//currency
		{:else if type == 'font'}
			//font
		{:else if type == 'range'}
			//range
		{:else if type == 'file'}
			//file
		{:else}
			... {type}
		{/if}
	</div>
{/if}

<script>
	import LocalizedText from '../LocalizedText.svelte';
	import NumberInput from './NumberInput.svelte';
	import Switch from './Switch.svelte';
	import ColorInput from './ColorInput.svelte';
	import SlimPassword from './SlimPassword.svelte';
	import SlimTextArea from './SlimTextArea.svelte';
	import SlimSelectMenu from './SlimSelectMenu.svelte';

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
					options={options.reduce((arr, i) => ({ ...arr, [i]: i }), {})}
				/>
			{:else if type == 'password'}
				<SlimPassword bind:value on:value={onInput} />
			{:else if type == 'currency'}
				//currency
			{:else if type == 'font'}
				//font
			{:else if type == 'range'}
				{@const { step, min, max } = widgetSettingsItem.extraData}
				<input class="range" type="range" bind:value on:input={onInput} {step} {min} {max} />
			{:else if type == 'file'}
				//file
			{:else}
				... {type}
			{/if}
		</div>
	</div>
{/if}

<style>
	.range {
		appearance: none;
		height: 4px !important;
		transform: translateY(-3px);
		background-color: var(--base6);
	}

	.range::-webkit-slider-runnable-track {
		width: 300px;
		height: 2px;
		border: none;
		border-radius: 3px;
	}

	.range::-webkit-slider-thumb {
		-webkit-appearance: none;
		border: none;
		height: 12px;
		width: 12px;
		border-radius: 500%;
		background: var(--base1);
		border: 2px solid var(--primary10);
		margin-top: -5px;
		cursor: pointer;
	}
</style>

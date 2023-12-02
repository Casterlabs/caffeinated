<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import NumberInput from './NumberInput.svelte';
	import Switch from './Switch.svelte';
	import ColorInput from './ColorInput.svelte';
	import SlimPassword from './SlimPassword.svelte';
	import SlimTextArea from './SlimTextArea.svelte';
	import SlimSelectMenu from './SlimSelectMenu.svelte';
	import RangeInput from './RangeInput.svelte';
	import FileInput from './FileInput.svelte';
	import SlimSearchMenu from './SlimSearchMenu.svelte';
	import CodeInput from './CodeInput.svelte';
	import PlatformDropdown from './PlatformDropdown.svelte';

	import { fonts, currencies } from '$lib/misc.mjs';

	export let widget;
	export let widgetSettingsSection;
	export let widgetSettingsItem;

	let isCodeInputExtended = false;

	const type = widgetSettingsItem.type.toLowerCase();
	const settingsKey = `${widgetSettingsSection.id}.${widgetSettingsItem.id}`;

	let value = widget.settings[settingsKey];

	if (value == undefined || value == null) {
		value = widgetSettingsItem.extraData.defaultValue;
	}

	function onInput() {
		Caffeinated.pluginIntegration.editWidgetSettingsItem(widget.id, settingsKey, value);
	}
</script>

{#if type == 'html'}
	{@html widgetSettingsItem.extraData.html}
{:else if type == 'checkbox'}
	<Switch title={widgetSettingsItem.name} description="" bind:checked={value} on:value={onInput} />
{:else}
	<!-- We have some custom CSS classes specifically for the code input. -->
	<div class="flex items-center justify-between w-full">
		<div class="flex flex-col" class:w-24={type == 'code'} class:pr-2={type == 'code'}>
			<p class="text-sm font-medium text-base-12">
				<LocalizedText key={widgetSettingsItem.name} />
			</p>
		</div>

		<div class="text-right w-40" class:flex-grow={type == 'code'}>
			{#if type == 'color'}
				<ColorInput bind:value on:value={onInput} />
			{:else if type == 'number'}
				{@const { step, min, max } = widgetSettingsItem.extraData}
				<NumberInput {step} {min} {max} bind:value on:value={onInput} />
			{:else if type == 'text'}
				<SlimTextArea rows="1" resize={false} bind:value on:value={onInput} />
			{:else if type == 'textarea'}
				<SlimTextArea rows="1" resize={true} bind:value on:value={onInput} />
			{:else if type == 'code'}
				{@const { language } = widgetSettingsItem.extraData}
				<div
					class="h-56 text-left border border-base-8 rounded-sm shadow relative"
					class:-ml-36={isCodeInputExtended}
					class:-mr-12={isCodeInputExtended}
					class:h-96={isCodeInputExtended}
				>
					<CodeInput {language} bind:value on:value={onInput} />
					<button
						class="absolute right-0 bottom-0 p-0.5 rounded-tl bg-base-6 hover:bg-base-7 border-t border-l border-base-8 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-base-12"
						on:click={() => (isCodeInputExtended = !isCodeInputExtended)}
					>
						{#if isCodeInputExtended}
							<icon class="h-5 w-5" data-icon="icon/arrows-pointing-in" />
						{:else}
							<icon class="h-5 w-5" data-icon="icon/arrows-pointing-out" />
						{/if}
					</button>
				</div>
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

							if (result.length == 20) {
								break; // Cap the results.
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
			{:else if type == 'platform_dropdown'}
				{@const { allowMultiple, requiredFeatures } = widgetSettingsItem.extraData}
				{@const realRequiredFeatures = requiredFeatures?.length > 0 ? requiredFeatures : null}
				<PlatformDropdown
					bind:value
					{allowMultiple}
					requiredFeatures={realRequiredFeatures}
					on:value={onInput}
				/>
			{:else}
				... {type}
			{/if}
		</div>
	</div>
{/if}

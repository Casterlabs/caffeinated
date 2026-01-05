<script lang="ts">
	import { getCurrencies } from '$lib/currencies';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { Input, Select } from '@casterlabs/ui';

	import { onMount, tick } from 'svelte';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	let { widget, settingsKey, settingsItem }: Props = $props();
	let container: HTMLDivElement;

	let selected = $state(widget.settings[settingsKey] as Awaited<typeof AppAuth.ALL>[]);
	let options: Awaited<typeof AppAuth.ALL>[] = $state([]);

	let { requiredFeatures, allowMultiple } = settingsItem.extraData as {
		requiredFeatures?: string[];
		allowMultiple?: boolean;
	};

	onMount(async () => {
		for (const [platform, features] of Object.entries(await Koi.features)) {
			if (!requiredFeatures || requiredFeatures.length == 0) {
				options.push(platform as any);
			} else {
				let meetsRequirements = true;
				for (const requiredFeature of requiredFeatures) {
					if (!features.includes(requiredFeature)) {
						meetsRequirements = false;
						break;
					}

					if (meetsRequirements) {
						options.push(platform as any);
					}
				}
			}
		}

		tick().then(() => {
			// I have no fucking idea.
			for (const input of container.querySelectorAll('input')) {
				const option = input.dataset.option!;
				const checked = selected.includes(option as any);
				input.checked = checked;
			}
		});
	});
</script>

<div class="contents" bind:this={container}>
	{#if allowMultiple}
		<div class="flex flex-col text-base-12" role="listbox" aria-multiselectable="true">
			{#each options as option}
				{@const isSelected = selected.includes(option)}

				<label>
					<LocalizedText key={`co.casterlabs.caffeinated.app.platform.${option}`} />

					<Input
						role="option"
						type="checkbox"
						aria-selected={isSelected}
						checked={isSelected}
						data-option={option}
						onchange={(e) => {
							const checked = (e.target as HTMLInputElement).checked;
							if (checked) {
								selected = [...selected, option];
							} else {
								selected = selected.filter((v) => v !== option);
							}
							AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, selected);
						}}
					/>
				</label>
			{/each}
		</div>
	{:else}
		<Select
			class="w-full"
			value={widget.settings[settingsKey] as string}
			onchange={(e) => {
				const value = (e.target as HTMLSelectElement).value;
				AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
			}}
		>
			{#each options as [optionValue]}
				<option value={optionValue}>
					<LocalizedText key={`co.casterlabs.caffeinated.app.platform.${optionValue}`} />
				</option>
			{/each}
		</Select>
	{/if}
</div>

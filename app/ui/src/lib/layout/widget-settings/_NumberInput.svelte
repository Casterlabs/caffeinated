<script lang="ts">
	import Debouncer from '$lib/debouncer';

	import { Button, Input } from '@casterlabs/ui';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	const debouncer = new Debouncer(150);

	let { widget, settingsKey, settingsItem }: Props = $props();

	let value = $state(widget.settings[settingsKey] as number);

	let isShowingNumberOverride = $state(false);

	let { step, min, max } = settingsItem.extraData as {
		step?: number;
		min?: number;
		max?: number;
	};
</script>

{#if settingsItem.type == 'RANGE'}
	<div class="flex flex-row items-center space-x-1">
		<Input
			class="w-full"
			type={isShowingNumberOverride ? 'number' : 'range'}
			bind:value
			{step}
			{min}
			{max}
			oninput={() => {
				debouncer.debounce(() => {
					AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
				});
			}}
			onchange={() => {
				AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
			}}
		/>

		<Button aria-hidden="true" borderless onclick={() => (isShowingNumberOverride = !isShowingNumberOverride)}>
			{#if isShowingNumberOverride}
				<svg class="text-base-11 size-6" fill="none" viewBox="0 0 18 4.5" stroke-width=".75" stroke="currentColor" version="1.1" xmlns="http://www.w3.org/2000/svg">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M 7.4999999,2.2499999 H 17.25 m -9.7500001,0 c 0,1.9999998 -2.9999999,1.9999998 -2.9999999,0 m 2.9999999,0 c 0,-1.99999985 -2.9999999,-1.99999985 -2.9999999,0 m -3.75,0 H 4.5"
						id="path1"
					/>
				</svg>
			{:else}
				<span class="text-base-11"> 123 </span>
			{/if}
		</Button>
	</div>
{:else}
	<Input
		class="w-full"
		type="number"
		bind:value
		{step}
		{min}
		{max}
		oninput={() => {
			debouncer.debounce(() => {
				AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
			});
		}}
		onchange={() => {
			AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
		}}
	/>
{/if}

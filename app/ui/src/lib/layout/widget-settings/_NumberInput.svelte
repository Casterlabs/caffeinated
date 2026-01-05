<script lang="ts">
	import Debouncer from '$lib/debouncer';

	import { Input } from '@casterlabs/ui';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	const debouncer = new Debouncer(150);

	let { widget, settingsKey, settingsItem }: Props = $props();

	let value = $state(widget.settings[settingsKey] as number);

	let { step, min, max } = settingsItem.extraData as {
		step?: number;
		min?: number;
		max?: number;
	};
</script>

<Input
	class="w-full"
	type={{
		NUMBER: 'number',
		RANGE: 'range'
	}[settingsItem.type as string]}
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

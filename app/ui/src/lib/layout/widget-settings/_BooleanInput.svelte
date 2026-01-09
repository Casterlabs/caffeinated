<script lang="ts">
	import { Input } from '@casterlabs/ui';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	let { widget, settingsKey, settingsItem }: Props = $props();

	let checked = $state(widget.settings[settingsKey] as boolean);
</script>

<Input
	type="checkbox"
	bind:checked
	onchange={() => {
		AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, checked);
	}}
/>

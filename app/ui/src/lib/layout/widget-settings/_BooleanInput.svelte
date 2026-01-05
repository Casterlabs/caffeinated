<script lang="ts">
	import { Input } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	let { widget, settingsKey, settingsItem }: Props = $props();
	let container: HTMLDivElement;

	let checked = $state(widget.settings[settingsKey] as boolean);

	onMount(() => {
		// I have no fucking idea.
		container.querySelector('input')!.checked = checked;
	});
</script>

<div class="contents" bind:this={container}>
	<Input
		type="checkbox"
		bind:checked
		onchange={() => {
			AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, checked);
		}}
	/>
</div>

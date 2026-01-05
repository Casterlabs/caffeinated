<script lang="ts">
	import Debouncer from '$lib/debouncer';

	import TextArea from '../TextArea.svelte';
	import { Input } from '@casterlabs/ui';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	const debouncer = new Debouncer(150);

	let { widget, settingsKey, settingsItem }: Props = $props();

	let value = $state(widget.settings[settingsKey] as string);
</script>

{#if settingsItem.type == 'TEXTAREA'}
	<TextArea
		class="w-full"
		bind:value
		oninput={() => {
			debouncer.debounce(() => {
				AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
			});
		}}
		onchange={() => {
			AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
		}}
	/>
{:else}
	<Input
		class="w-full"
		type={{
			COLOR: 'color',
			TEXT: 'text',
			PASSWORD: 'password'
		}[settingsItem.type as string]}
		bind:value
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

<style>
	:global(input[type='color']) {
		padding: 0 !important;
	}
</style>

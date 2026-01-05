<script lang="ts">
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import CodeEditor from './CodeEditor.svelte';
	import BooleanInput from './_BooleanInput.svelte';
	import DropdownInput from './_DropdownInput.svelte';
	import FileInput from './_FileInput.svelte';
	import NumberInput from './_NumberInput.svelte';
	import PlatformInput from './_PlatformInput.svelte';
	import TextInput from './_TextInput.svelte';

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsSection: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0];
	}

	let { widget, settingsSection }: Props = $props();
</script>

<ul class="flex-1 block w-full mt-2 divide-y divide-current text-base-6">
	{#each settingsSection?.items || [] as item (settingsSection.id + '.' + item.id)}
		{@const settingsKey = settingsSection.id + '.' + item.id}
		{@const isCodeEditor = item.type == 'CODE'}
		{@const isTextArea = item.type == 'TEXTAREA'}

		<!-- NB: Code editor gets to become extra wide. TextArea does NOT, but it does get to split the line from it's label -->

		<li class="py-4 mx-auto" class:max-w-sm={!isCodeEditor} class:max-w-2xl={isCodeEditor}>
			<label class="flex items-center justify-between w-full mx-auto" class:flex-col={isCodeEditor || isTextArea}>
				<div class="flex flex-col" class:mb-2={isCodeEditor || isTextArea}>
					<p class="text-sm font-medium text-base-12">
						<LocalizedText key={item.name} />
					</p>
				</div>

				<div class="text-right" class:w-40={!isCodeEditor && !isTextArea} class:w-full={isCodeEditor || isTextArea}>
					{#if item.type == 'CHECKBOX'}
						<BooleanInput {widget} {settingsKey} settingsItem={item} />
					{:else if item.type == 'COLOR' || item.type == 'TEXT' || item.type == 'PASSWORD' || item.type == 'TEXTAREA'}
						<TextInput {widget} {settingsKey} settingsItem={item} />
					{:else if item.type == 'NUMBER' || item.type == 'RANGE'}
						<NumberInput {widget} {settingsKey} settingsItem={item} />
					{:else if item.type == 'DROPDOWN' || item.type == 'CURRENCY' || item.type == 'FONT'}
						<DropdownInput {widget} {settingsKey} settingsItem={item} />
					{:else if item.type == 'PLATFORM_DROPDOWN'}
						<PlatformInput {widget} {settingsKey} settingsItem={item} />
					{:else if item.type == 'CODE'}
						{@const { language } = item.extraData}
						<div class="w-full h-96 text-left border border-base-8 rounded-sm relative">
							<CodeEditor {language} value={widget.settings[settingsKey]} onchange={(value) => AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value)} />
						</div>
					{:else if item.type == 'FILE'}
						<FileInput {widget} {settingsKey} settingsItem={item} />
					{/if}
				</div>
			</label>
		</li>
	{/each}
</ul>

<style>
	:global(input[type='color']) {
		padding: 0 !important;
	}
</style>

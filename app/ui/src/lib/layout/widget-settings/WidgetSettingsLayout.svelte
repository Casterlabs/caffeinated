<script lang="ts">
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
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

<ul class="flex-1 block w-full max-w-sm mx-auto mt-2 divide-y divide-current text-base-6">
	{#each settingsSection?.items || [] as item (settingsSection.id + '.' + item.id)}
		{@const settingsKey = settingsSection.id + '.' + item.id}
		{@const isWeirdSize = item.type == 'CODE' || item.type == 'TEXTAREA'}

		<li class="py-4">
			<label class="flex items-center justify-between w-full">
				<div class="flex flex-col" class:w-24={isWeirdSize} class:pr-2={isWeirdSize}>
					<p class="text-sm font-medium text-base-12">
						<LocalizedText key={item.name} />
					</p>
				</div>

				<div class="text-right" class:flex-grow={isWeirdSize} class:w-40={!isWeirdSize}>
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
						CodeInput
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

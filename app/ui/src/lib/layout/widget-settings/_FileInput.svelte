<script lang="ts">
	import { IconXMark } from '@casterlabs/heroicons-svelte';

	const FILE_SIZE_THRESHOLD = 1048576 * 10; // 10mb

	interface Props {
		widget: Awaited<typeof AppPlugins.widgets>[0];
		settingsKey: string;
		settingsItem: Awaited<typeof AppPlugins.widgets>[0]['settingsLayout']['sections'][0]['items'][0];
	}

	let { widget, settingsKey, settingsItem }: Props = $props();

	let value = $state(widget.settings[settingsKey] as string);
	let { allowed: allowedTypes } = settingsItem.extraData as {
		allowed?: string[];
	};

	let header = $derived(value?.substring(0, value.indexOf('base64,')));

	function getFileName() {
		if (header.includes('filename=')) {
			let filename = header;

			filename = filename.substring(filename.indexOf('filename=') + 'filename='.length);
			filename = filename.substring(0, filename.indexOf(';'));

			return decodeURIComponent(filename);
		} else {
			return 'Unknown File Name';
		}
	}

	function getFileMime() {
		return header.substring('data:'.length, header.indexOf(';'));
	}

	async function pickFile() {
		const file = await App.pickFile([]);
		if (!file) return;

		const approximateSize = (file.split(',', 2)[1].length * 3) / 4;

		if (approximateSize > FILE_SIZE_THRESHOLD) {
			if (approximateSize > FILE_SIZE_THRESHOLD) {
				if (
					confirm(
						`The current selected file is greater than 10mb (Approximate Size: ${fileSizeFormatter(
							approximateSize,
							1
						)}) which is known to cause issues.\n\nEither click OK to proceed or click cancel to select a smaller file.`
					)
				) {
					console.debug("User OK'd a large file read.");
				} else {
					console.debug('User aborted a large file read.');
					return;
				}
			}
		}

		value = file;
		AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
	}

	function clearInput() {
		setTimeout(() => {
			value = '';
			AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);
		}, 100);
	}

	function fileSizeFormatter(num: number, decimalPlaces = 1, threshold = 1000) {
		let shortened = '';
		let mult = '';

		if (num >= threshold && num >= 1000) {
			if (num >= 1099511627776) {
				shortened = 'Over 1';
				mult = 'tb';
			} else if (num >= 1073741824) {
				shortened = (num / 1000000000).toFixed(decimalPlaces);
				mult = 'gb';
			} else if (num >= 1048576) {
				shortened = (num / 1000000).toFixed(decimalPlaces);
				mult = 'mb';
			} else if (num >= 1024) {
				shortened = (num / 1000).toFixed(decimalPlaces);
				mult = 'kb';
			}
		} else {
			shortened = num.toFixed(decimalPlaces);
			mult = 'b';
		}

		if (shortened.includes('.')) {
			shortened = shortened.replace(/\.?0+$/, '');
		}

		return shortened + mult;
	}
</script>

<div class="flex flex-row float-right text-base-12">
	<button
		type="button"
		class="relative w-fit truncate h-[2.375rem] cursor-pointer rounded-l-md -mr-px py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
		class:rounded-md={!value}
		onclick={pickFile}
	>
		{#key value}
			{#if value}
				{getFileName()}
			{:else}
				Choose a file...
			{/if}
		{/key}
	</button>
	{#if value}
		<button
			type="button"
			class="relative w-fit h-[2.375rem] cursor-pointer rounded-r-md py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
			onclick={clearInput}
		>
			<IconXMark />
			<span class="sr-only">Clear file input</span>
		</button>
	{/if}
</div>

{#key value}
	{#if value}
		{@const mime = getFileMime()}
		<div aria-hidden="true" class="block mx-auto w-fit mt-11">
			{#if mime.startsWith('image/')}
				<img class="max-w-full max-h-20 contain" src={value} alt="" />
			{:else if mime.startsWith('audio/')}
				<audio class="max-w-full h-10" src={value} controls controlslist="nodownload" volume={0.5}></audio>
			{:else if mime.startsWith('video/')}
				<!-- svelte-ignore a11y_media_has_caption -->
				<video class="max-w-full max-h-20 contain" controls src={value}></video>
			{/if}
		</div>
	{/if}
{/key}

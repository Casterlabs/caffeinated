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

	let blanking = $state(false);
	let fileInput: HTMLInputElement | null = $state(null);

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

	async function onChange() {
		const file = fileInput!.files![0];
		const result = await fileToBase64(file);

		value = result;
		AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);

		recreateDom();
	}

	function clearInput() {
		value = '';
		AppPlugins.editWidgetSettingsItem(widget!.id, settingsKey, value);

		recreateDom();
	}

	function recreateDom() {
		blanking = true;
		setTimeout(() => (blanking = false), 100); // Recreate the dom (clears the input)
	}

	function fileToBase64(file: File): Promise<string> {
		return new Promise((resolve) => {
			// Check the file type.
			const filetype = file.type;

			if (allowedTypes) {
				let okay = false;

				for (const type of allowedTypes) {
					if (filetype.startsWith(type)) {
						okay = true;
						break;
					}
				}

				if (!okay) {
					resolve('');
					return;
				}
			}

			// Check the file size.
			const size = file.size;

			if (size > FILE_SIZE_THRESHOLD) {
				if (
					confirm(
						`The current selected file is greater than 10mb (Actual Size: ${fileSizeFormatter(
							size,
							1
						)}) which is known to cause issues.\n\nEither click OK to proceed or click cancel to select a smaller file.`
					)
				) {
					console.debug("User OK'd a large file read.");
				} else {
					resolve('');
					fileInput!.value = '';
					console.debug('User aborted a large file read.');
					return;
				}
			}

			console.debug(`Reading a ${fileSizeFormatter(size, 1)} file. (${filetype})`);

			try {
				const reader = new FileReader();

				reader.readAsDataURL(file);
				reader.onload = () => {
					const filename = file.name;
					let result = reader.result! as string;

					result = result.substring(result.indexOf(';') + 1);

					// We sneak in our own filename property :^)
					// God, I am so fucking big brained it hurts.
					resolve(`data:${filetype};filename=${encodeURIComponent(filename)};${result}`);
				};
			} catch (e) {
				console.warn(e);
				resolve('');
				fileInput!.value = '';
			}
		});
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

{#if !blanking}
	<input class="hidden" type="file" bind:this={fileInput} oninput={onChange} />
{/if}

<div class="flex flex-row float-right text-base-12">
	<button
		type="button"
		class="relative w-fit truncate h-[2.375rem] cursor-pointer rounded-l-md -mr-px py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
		class:rounded-md={!value}
		onclick={() => fileInput!.click()}
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
				<video class="max-w-full max-h-20 contain" controls src={value.replace(`;filename=${getFileName()}`, '')}></video>
			{/if}
		</div>
	{/if}
{/key}

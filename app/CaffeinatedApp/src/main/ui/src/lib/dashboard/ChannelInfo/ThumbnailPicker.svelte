<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import Aspect16by9 from '$lib/aspect-ratio/Aspect16by9.svelte';

	import { onDestroy, onMount, tick } from 'svelte';
	import { t } from '$lib/app.mjs';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('ThumbnailPicker');

	const ALLOWED_TYPES = 'image/png,image/jpg,image/jpeg,image/gif';

	let blanking = false;
	let fileInput;

	export let values;
	let previewUrl;

	async function onChange() {
		const file = fileInput.files[0];
		// const result = await fileToBase64(file, allowedTypes);

		values.thumbnail = file;

		// try {
		// 		const reader = new FileReader();

		// 		reader.readAsBinaryString
		// 		reader.readAsDataURL(file);
		// 		reader.onload = () => {
		// 			const filename = file.name;
		// 			let result = reader.result;

		// 			result = result.substring(result.indexOf(';') + 1);

		// 			// We sneak in our own filename property :^)
		// 			// God, I am so fucking big brained it hurts.
		// 			resolve(`data:${filetype};filename=${filename};${result}`);
		// 		};
		// 	} catch (e) {
		// 		console.warn(e);
		// 		resolve('');
		// 		fileElement.value = '';
		// 	}

		console.log(file);

		// value = result;

		recreateDom();
	}

	function clearInput() {
		values.thumbnail = null;
		recreateDom();
	}

	function recreateDom() {
		if (previewUrl != values.thumbnail_url) {
			URL.revokeObjectURL(previewUrl);
			console.debug('Revoked object url.');
		}

		if (values.thumbnail) {
			previewUrl = URL.createObjectURL(values.thumbnail);
			console.debug('Created object url.');
		} else {
			previewUrl = values.thumbnail_url;
		}

		blanking = true;
		tick().then(() => (blanking = false)); // Recreate the dom (clears the input)
	}

	onMount(recreateDom);

	onDestroy(() => {
		if (previewUrl != values.thumbnail_url) {
			URL.revokeObjectURL(previewUrl);
		}
	});
</script>

{#if !blanking}
	<input
		class="hidden"
		type="file"
		bind:this={fileInput}
		on:input={onChange}
		accept={ALLOWED_TYPES}
	/>
{/if}

<Aspect16by9>
	<div class="w-full h-full relative rounded-md overflow-hidden shadow-sm">
		<img class="w-full h-full contain" src={previewUrl} alt="" />

		<div class="absolute bottom-1 right-1 shadow-sm">
			<button
				type="button"
				class="relative w-fit truncate h-8 cursor-pointer rounded-l-md -mr-px py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
				class:rounded-md={!values.thumbnail}
				on:click={() => fileInput.click()}
			>
				<LocalizedProperty
					key="co.casterlabs.caffeinated.app.docks.channel_info.thumbnail.select_file"
					property="title"
				/>
				<span class="sr-only">
					<LocalizedText
						key="co.casterlabs.caffeinated.app.docks.channel_info.thumbnail.select_file"
					/>
				</span>
				<icon class="w-4 h-4" data-icon="icon/arrow-up-tray" />
			</button>
			{#if values.thumbnail}
				<button
					type="button"
					class="-ml-1 relative w-fit h-8 cursor-pointer rounded-r-md py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
					on:click={clearInput}
				>
					<LocalizedProperty
						key="co.casterlabs.caffeinated.app.docks.channel_info.thumbnail.clear"
						property="title"
					/>
					<span class="sr-only">
						<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.thumbnail.clear" />
					</span>
					<icon class="w-4 h-4" data-icon="icon/x-mark" />
				</button>
			{/if}
		</div>
	</div>
</Aspect16by9>

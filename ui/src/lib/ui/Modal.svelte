<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { createEventDispatcher } from 'svelte';
	import { t } from '$lib/translate.mjs';
	import { fade } from 'svelte/transition';

	const dispatch = createEventDispatcher();

	function close() {
		dispatch('close');
	}
</script>

<div
	class="relative z-10"
	aria-labelledby="modal-title"
	role="dialog"
	aria-modal="true"
	transition:fade={{ duration: 150 }}
>
	<div class="fixed inset-0 bg-base-6 opacity-75 transition-opacity" />

	<div class="fixed inset-0 z-10 overflow-y-auto">
		<div class="flex min-h-full items-center justify-center text-center">
			<div
				class="relative transform overflow-hidden rounded-lg bg-base-1 text-left shadow-xl transition-all px-6 pb-5 pt-3.5 w-full max-w-3xl"
			>
				<h3 class="text-lg font-medium text-base-12 mb-4" id="modal-title">
					<slot name="title" />
				</h3>

				<button
					class="absolute top-4 right-3.5 text-base-12"
					title={t('modal.close')}
					on:click={close}
				>
					<span class="sr-only">
						<LocalizedText key="sr.modal.close" />
					</span>
					<icon data-icon="icon/x-mark" />
				</button>

				<div class="text-base-10">
					<slot />
				</div>
			</div>
		</div>
	</div>
</div>

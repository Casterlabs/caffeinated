<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';

	import { createEventDispatcher } from 'svelte';
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
				class="relative transform overflow-hidden rounded-lg bg-base-1 text-left shadow-xl transition-all px-6 pb-5 pt-3.5 w-fit max-w-3xl"
			>
				<h3 class="text-lg font-medium text-base-12 mb-4" id="modal-title">
					<slot name="title" />
				</h3>

				<button class="absolute top-4 right-3.5 text-base-12" on:click={close}>
					<LocalizedProperty key="co.casterlabs.caffeinated.app.ui.modal.close" property="title" />
					<span class="sr-only">
						<LocalizedText key="co.casterlabs.caffeinated.app.ui.modal.close" />
					</span>
					<icon data-icon="icon/x-mark" />
				</button>

				<div class="text-base-11">
					<slot />
				</div>
			</div>
		</div>
	</div>
</div>

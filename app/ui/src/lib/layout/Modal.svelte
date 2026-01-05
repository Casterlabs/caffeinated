<script lang="ts">
	import { fade } from 'svelte/transition';

	import { IconXMark } from '@casterlabs/heroicons-svelte';

	import type { Snippet } from 'svelte';

	interface Props {
		title: Snippet;
		children?: Snippet;
		onclose: () => void;
	}

	let { title, children, onclose }: Props = $props();
</script>

<div class="modal absolute inset-0 flex items-center justify-center" transition:fade={{ duration: 75 }} role="alertdialog">
	<div class="text-base-12 bg-base-1 border border-base-4 rounded-lg max-w-96 w-[80%] relative z-50">
		<div class="flex items-center justify-center text-sm border-b border-base-4 pt-2 p-1.5">
			{@render title()}
		</div>

		<div class="px-2 pt-3 pb-4">
			{@render children?.()}
		</div>

		<button class="absolute top-1 right-1" title="Close" onclick={() => onclose()}>
			<IconXMark theme="solid" />
		</button>
	</div>

	<!-- svelte-ignore a11y_click_events_have_key_events -->
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div class="absolute modal-backdrop inset-0 z-40" onclick={() => onclose()} aria-hidden={true}></div>
</div>

<style>
	.modal-backdrop {
		background: color-mix(in srgb, var(--base1) 80%, transparent);
	}
</style>

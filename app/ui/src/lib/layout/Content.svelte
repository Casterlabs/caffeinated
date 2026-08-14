<script lang="ts">
	// @ts-ignore
	const isDock = typeof window.Widget !== 'undefined';
</script>

<main id="main" class="flex-1 h-full overflow-auto" class:px-6={!isDock} class:py-4={!isDock}>
	<!-- svelte-ignore slot_element_deprecated -->
	<slot />
</main>

<script lang="ts">
	import { glocale, lookup, rerenderKey } from '$lib/locale/locale';

	import type { Snippet } from 'svelte';

	declare interface ComponentPart {
		readonly componentName: string;
	}

	interface Props {
		key: string;
		args?: Record<string, any>;
		components?: Record<string, Snippet>;
	}

	let { key, args = {}, components = {} }: Props = $props();
</script>

{#key $rerenderKey && key && args && components}
	{#each lookup(key) as part}
		{#if part.type == 'COMPONENT'}
			{@const componentPart = part as ComponentPart}
			{@const snippet = components[componentPart.componentName]}
			{#if typeof snippet == 'function'}
				{@render snippet()}
			{/if}
		{:else}
			{@html part.render(glocale, args)}
		{/if}
	{/each}
{/key}

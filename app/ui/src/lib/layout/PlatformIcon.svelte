<script lang="ts">
	interface Props {
		class?: string;
		style?: string;

		platform: string;
		color?: boolean;
	}

	let { class: className, style, platform, color = false }: Props = $props();

	let src = $derived(`/$caffeinated-sdk-root$/images/services/${platform.toLowerCase()}/${color ? 'logo.png' : 'icon.svg'}`);
</script>

{#if color}
	<img style="display: inline; width: auto; height: 1em; vertical-align: middle; {style}" class={className} {src} alt="" />
{:else}
	{#await fetch(src).then((res) => res.text()) then svgText}
		{@html svgText.replace('<svg', `<svg style="width: 1em; height: 1em; ${style}" class="${className}" `)}
	{/await}
{/if}

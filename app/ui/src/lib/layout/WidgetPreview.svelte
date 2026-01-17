<script lang="ts">
	import LocalizedText from '$lib/locale/LocalizedText.svelte';

	interface Props {
		ariaHidden?: boolean;
		widget: Awaited<typeof AppPlugins.widgets>[0];
		mode?: 'WIDGET' | 'WIDGET_ALT' | 'DEMO' | 'DOCK' | 'APPLET' | 'SETTINGS_APPLET';
	}

	let { ariaHidden = true, widget, mode }: Props = $props();

	let url = $derived(mode ? widget.url.replace(/&mode=\w+/, `&mode=${mode}`) : widget.url);
</script>

{#if url}
	{#if mode == 'DOCK' || mode == 'APPLET' || mode == 'SETTINGS_APPLET'}
		<iframe class="flex-grow w-full h-full dock-widget-preview" aria-hidden={ariaHidden} title="" src={url}></iframe>
	{:else}
		<div class="max-w-md mx-auto mt-6 mb-8">
			<h1 class="font-semibold text-xl">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.preview" />
			</h1>

			<div class="w-full mt-1 select-none border border-base-8 shadow-xl rounded-md">
				<div class="aspect-container" style:--aspect={widget.details.demoAspectRatio}>
					<div>
						<iframe class="w-full h-full" aria-hidden={ariaHidden} title="" src={url}></iframe>
					</div>
				</div>
			</div>
		</div>
	{/if}
{/if}

<style>
	.aspect-container {
		position: relative;
		padding-bottom: calc(var(--aspect) * 100%);
		overflow: hidden;
	}
	.aspect-container > div {
		position: absolute;
		top: 0;
		left: 0;
		height: 100%;
		width: 100%;
	}
</style>

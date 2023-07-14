<script>
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';

	let html = '';
	let highlightColor;
	let textColor;
	let fontSize;
	let textAlign;

	onMount(() => {
		Widget.on('update', () => {
			changeFont(Widget.getSetting('text_style.font'));
			highlightColor = Widget.getSetting('text_style.highlight_color');
			textColor = Widget.getSetting('text_style.text_color');
			fontSize = Widget.getSetting('text_style.font_size');
			textAlign = Widget.getSetting('text_style.text_align');
		});

		Widget.on('html', ({ html: h }) => {
			console.log(h);
			html = h;
		});

		Widget.on('init', () => Widget.broadcast('update'));
	});
</script>

<span
	style:--highlight-color={highlightColor}
	style:color={textColor}
	style:font-size="{fontSize}px"
	style:text-align={textAlign}
>
	{@html html}
</span>

<style>
	:global(.highlight) {
		color: var(--highlight-color);
	}
</style>

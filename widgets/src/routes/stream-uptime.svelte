<script>
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';

	let html = '';
	let highlightColor;
	let textColor;
	let fontSize;
	let textAlign;

	function updateText() {
		requestAnimationFrame(updateText);

		let streamdata;

		for (const state of Object.values(Koi.streamStates)) {
			if (state && state.is_live) {
				// Try to find the oldest timestamp.
				if (streamdata && state.start_time > streamdata.start_time) {
					continue;
				}

				streamdata = state;
			}
		}

		const delta = streamdata ? new Date() - new Date(streamdata.start_time) : 0;

		const prefix = Widget.getSetting('text.prefix');
		const suffix = Widget.getSetting('text.suffix');

		let text = new Date(delta).toISOString().substring(11, 11 + 8);

		if (prefix.length > 0) {
			text = prefix + ' ' + text;
		}

		if (suffix.length > 0) {
			text = text + ' ' + suffix;
		}

		html = text;
	}

	onMount(() => {
		updateText();

		Widget.on('update', () => {
			changeFont(Widget.getSetting('text_style.font'));
			highlightColor = Widget.getSetting('text_style.highlight_color');
			textColor = Widget.getSetting('text_style.text_color');
			fontSize = Widget.getSetting('text_style.font_size');
			textAlign = Widget.getSetting('text_style.text_align');
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

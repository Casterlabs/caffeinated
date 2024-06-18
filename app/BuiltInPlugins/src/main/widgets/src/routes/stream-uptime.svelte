<script>
	import { writable } from 'svelte/store';
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';

	const settings = writable({});

	let html = '';

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
			settings.set(Widget.widgetData.settings);
			changeFont(Widget.getSetting('text_style.font'));
		});

		Widget.on('init', () => Widget.broadcast('update'));
	});
</script>

<span
	style:--highlight-color={$settings['text_style.highlight_color']}
	style:color={$settings['text_style.text_color']}
	style:font-size="{$settings['text_style.font_size']}px"
	style:font-weight={$settings['text_style.font_weight']}
	style:text-align={$settings['text_style.text_align']}
	style:-webkit-text-stroke="{$settings['text_style.outline_width'] * 0.1}em {$settings[
		'text_style.outline_color'
	]}"
	style:filter={$settings['text_style.text_shadow'] == -1
		? ''
		: `drop-shadow(0px 0px ${$settings['text_style.text_shadow']}px black)`}
	style:display="block"
>
	{@html html}
</span>

<style>
	:global(.highlight) {
		color: var(--highlight-color);
	}
</style>

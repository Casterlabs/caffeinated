<script>
	import AspectVar from '$lib/aspect-ratio/AspectVar.svelte';

	import { writable } from 'svelte/store';
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';
	import getAverageColor from '$lib/getAverageColor.mjs';

	let title;
	let artist;
	let albumArtUrl;

	const settings = writable({});
	let cardColor;

	async function update() {
		console.log(Music.activePlayback);

		if (!Music.activePlayback) {
			title = '';
			artist = '';
			return;
		}

		title = Music.activePlayback.currentTrack.title;
		artist = Music.activePlayback.currentTrack.artists.join(', ');
		albumArtUrl = Music.activePlayback.currentTrack.albumArtUrl;

		if (cardStyle == 'Text Only') {
			changeFont(textOnly_font);
		} else {
			changeFont('Poppins');
		}

		if (backgroundStyle == 'Solid') {
			getAverageColor(Music.activePlayback.currentTrack.albumArtUrl).then(([r, g, b]) => {
				cardColor = `rgb(${r}, ${g}, ${b})`;
			});
		} else {
			cardColor = 'transparent';
		}
	}

	onMount(() => {
		update();
		Widget.on('init', update);
		Music.on('music', update);
		Widget.on('update', () => {
			settings.set(Widget.widgetData.settings);
			update();
		});
	});
</script>

<!-- svelte-ignore a11y-missing-attribute -->
{#if title}
	{#if $settings['style.card_style'] == 'Horizontal Card'}
		<div class="drop-shadow-lg">
			<AspectVar aspectRatio={298 / 930}>
				<div
					class="w-full h-full overflow-hidden rounded-lg relative shadow-lg"
					style:background-color={cardColor}
					style:margin="{$settings['style.margin']}px"
				>
					{#if $settings['style.background_style'] == 'Blur'}
						<img
							class="absolute inset-0 w-full h-full object-cover blur-sm brightness-50"
							src={albumArtUrl}
						/>
					{/if}

					<div class="absolute inset-4 flex flex-row space-x-6">
						{#if $settings['style.image_style'] == 'Left'}
							<img class="rounded-md drop-shadow-lg" src={albumArtUrl} />
						{/if}
						<div class="flex-1 text-white">
							<h1 class="text-[5vw] drop-shadow-2xl">{title}</h1>
							<h2 class="text-[3vw] drop-shadow-2xl">{artist}</h2>
						</div>
						{#if $settings['style.image_style'] == 'Right'}
							<img class="rounded-md drop-shadow-lg" src={albumArtUrl} />
						{/if}
					</div>
				</div>
			</AspectVar>
		</div>
	{:else if $settings['style.card_style'] == 'Text Only'}
		<p
			style:font-size="{$settings['style.font_size']}px"
			style:font-weight={$settings['style.font_weight']}
			style:text-align={$settings['style.text_align']}
			style:color={$settings['style.text_color']}
			style:-webkit-text-stroke="{$settings['style.outline_width'] * 0.1}em {$settings[
				'style.outline_color'
			]}"
			style:filter={$settings['style.text_shadow'] == -1
				? ''
				: `drop-shadow(0px 0px ${$settings['style.text_shadow']}px black)`}
		>
			{title} &bull; {artist}
		</p>
	{:else if $settings['style.card_style'] == 'Image Only'}
		<img
			class="absolute inset-0 w-full h-full object-contain"
			src={albumArtUrl}
			style:padding="{$settings['style.margin']}px;"
		/>
	{/if}
{/if}

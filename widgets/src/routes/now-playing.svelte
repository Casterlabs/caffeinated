<script>
	import AspectVar from '$lib/aspect-ratio/AspectVar.svelte';

	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';
	import getAverageColor from '$lib/getAverageColor.mjs';

	let title;
	let artist;
	let albumArtUrl;

	let cardStyle;
	let cardColor;
	let textColor;
	let backgroundStyle;
	let imageStyle;
	let margin;

	let textOnly_font;
	let textOnly_fontSize;
	let textOnly_textColor;
	let textOnly_textAlign;

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

		cardStyle = Widget.getSetting('style.card_style');
		textColor = Widget.getSetting('style.text_color');
		backgroundStyle = Widget.getSetting('style.background_style');
		imageStyle = Widget.getSetting('style.image_style');
		margin = Widget.getSetting('style.margin');

		textOnly_font = Widget.getSetting('style.font');
		textOnly_fontSize = Widget.getSetting('style.font_size');
		textOnly_textColor = Widget.getSetting('style.text_color');
		textOnly_textAlign = Widget.getSetting('style.text_align');

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
		Widget.on('update', update);
		Music.on('music', update);
	});
</script>

<!-- svelte-ignore a11y-missing-attribute -->
{#if title}
	{#if cardStyle == 'Horizontal Card'}
		<div class="drop-shadow-lg">
			<AspectVar aspectRatio={298 / 930}>
				<div
					class="w-full h-full overflow-hidden rounded-lg relative shadow-lg"
					style:background-color={cardColor}
					style:margin="{margin}px"
				>
					{#if backgroundStyle == 'Blur'}
						<img
							class="absolute inset-0 w-full h-full object-cover blur-sm brightness-50"
							src={albumArtUrl}
						/>
					{/if}

					<div class="absolute inset-4 flex flex-row space-x-6">
						{#if imageStyle == 'Left'}
							<img class="rounded-md drop-shadow-lg" src={albumArtUrl} />
						{/if}
						<div class="flex-1 text-white">
							<h1 class="text-[5vw] drop-shadow-2xl">{title}</h1>
							<h2 class="text-[3vw] drop-shadow-2xl">{artist}</h2>
						</div>
						{#if imageStyle == 'Right'}
							<img class="rounded-md drop-shadow-lg" src={albumArtUrl} />
						{/if}
					</div>
				</div>
			</AspectVar>
		</div>
	{:else if cardStyle == 'Text Only'}
		<p
			style:font-size="{textOnly_fontSize}px"
			style:text-align={textOnly_textAlign}
			style:color={textOnly_textColor}
		>
			{title} &bull; {artist}
		</p>
	{:else if cardStyle == 'Image Only'}
		<img
			class="absolute inset-0 w-full h-full object-contain"
			src={albumArtUrl}
			style:padding="{margin}px;"
		/>
	{/if}
{/if}

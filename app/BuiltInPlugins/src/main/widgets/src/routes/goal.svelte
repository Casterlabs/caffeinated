<script>
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';

	let style;
	let showNumbers;
	let roundness;

	let textHtml = '';
	let titleHtml = '';
	let titleCountHtml = '';
	let progress = 0;

	let barColor;
	let textColor;
	let fontSize;
	let fontWeight;
	let textAlign;
	let margin;
	let shadow;
	let outline;

	let currentCount = -1;

	async function render() {
		if (currentCount == -1) {
			return;
		}

		const IS_DONATION_RELATED = Widget.widgetData.namespace == 'co.casterlabs.donation_goal';

		// We have to convert from the internal USD value to the currency so that the target is correct and the bar shows the right progress.
		const currentCountNative = IS_DONATION_RELATED
			? await Currencies.convertCurrency(
					currentCount,
					'USD',
					Widget.getSetting('money.currency'),
					false
			  )
			: currentCount;

		let title = Widget.getSetting('goal.title');
		let targetCount = Widget.getSetting('goal.target') || 0;

		if (title.length > 0) {
			title += ' '; // Add a space.
		}

		titleHtml = escapeHtml(title);
		progress = currentCountNative / targetCount;

		console.debug(currentCountNative, targetCount);

		// We need to adjust the text for the donation goal.
		if (IS_DONATION_RELATED) {
			const displayedCurrentCount = await Currencies.formatCurrency(
				currentCountNative,
				Widget.getSetting('money.currency')
			);
			const displayedTargetCount = await Currencies.formatCurrency(
				targetCount,
				Widget.getSetting('money.currency')
			);

			textHtml = `${escapeHtml(title)}&nbsp;&nbsp;${displayedCurrentCount}/${displayedTargetCount}`;
			titleCountHtml = `${displayedCurrentCount}/${displayedTargetCount}`;
		} else {
			textHtml = `${title}${currentCountNative}/${targetCount}`;
			titleCountHtml = `${currentCountNative}/${targetCount}`;
		}
	}

	onMount(() => {
		Widget.on('update', () => {
			changeFont(Widget.getSetting('style.font'));

			style = Widget.getSetting('style.style');
			showNumbers = Widget.getSetting('goal.add_numbers');
			roundness = Widget.getSetting('goal.rounded_edges') ? Widget.getSetting('goal.roundness') : 0;

			console.log(Widget.widgetData.settings);

			barColor = Widget.getSetting('goal.bar_color');
			textColor = Widget.getSetting('style.text_color');
			fontSize = Widget.getSetting('style.font_size');
			fontWeight = Widget.getSetting('text_style.font_weight');
			textAlign = Widget.getSetting('style.text_align') || 'left';
			margin = Widget.getSetting('style.margin');

			outline = `${Widget.getSetting('style.outline_width') * 0.1}em ${Widget.getSetting(
				'style.outline_color'
			)};`;

			if (style == 'Text Only') {
				shadow = Widget.getSetting('style.text_shadow');
			} else {
				shadow = -1;
			}

			render();
		});

		Widget.on('count', ({ count }) => {
			currentCount = count;
			render();
		});

		Widget.on('init', () => Widget.broadcast('update'));
	});
</script>

<div
	style:color={textColor}
	style:font-size="{fontSize}px"
	style:padding="{margin}px"
	style:font-weight={fontWeight}
	style:-webkit-text-stroke={outline}
	style:filter={shadow == -1 ? '' : `drop-shadow(0px 0px ${shadow}px black)`}
>
	{#if currentCount != -1}
		{#if style == 'Text Only'}
			<p style:text-align={textAlign}>
				{@html textHtml}
			</p>
		{:else}
			<div
				class="w-full h-12 p-1"
				style:border-radius="{roundness}px"
				style:background-color="rgba(1, 1, 1, 0.5)"
			>
				<div class="w-full h-full relative overflow-hidden">
					<div
						class="absolute inset-0"
						style:border-radius="{roundness}px"
						style:width="{progress * 100}%"
						style:background-color={barColor}
					/>

					<div class="absolute inset-0 flex justify-center items-center">
						<p class="titlespan">
							<span>
								{@html titleHtml}
							</span>
							{#if showNumbers}
								<span>
									{@html titleCountHtml}
								</span>
							{/if}
						</p>
					</div>
				</div>
			</div>
		{/if}
	{/if}
</div>

<style>
	:global(.titlespan svg),
	:global(.titlespan img) {
		display: inline-block;
		transform: unset !important;
	}
</style>

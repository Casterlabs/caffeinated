<script>
	import { onMount } from 'svelte';

	let timeout = 10 * 1000;
	let maxEmojis = 1000;
	let size = 15;
	let speed = 1;

	const minOpacity = 0.8;
	let avgTickTime = 0;
	let emojis = [];
	let canvas;
	let ctx;

	const targetDelta = 1000 / 60; // 60hz
	let start = performance.now();

	function create(content) {
		if (emojis.length < maxEmojis) {
			const scale = Math.random() * (1 - minOpacity) + minOpacity;

			if (content.startsWith('http')) {
				const image = new Image();
				image.src = content;
				content = image;
			}

			const emoji = {
				x: Math.random() * canvas.width,
				y: Math.random() * canvas.height,
				ys: Math.random() + 2,
				scale: scale,
				opacity: 0.01,
				content: content,
				dying: false,
				time: performance.now()
			};

			emojis.push(emoji);
		}
	}

	function draw() {
		const delta = performance.now() - start;
		const deltaRate = delta / targetDelta;

		start += delta;

		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.fillStyle = 'white';

		if (emojis.length > 0) {
			emojis.forEach((emoji) => {
				ctx.save();

				emoji.y += emoji.ys * deltaRate * speed;

				const relativeSize = size * emoji.scale;

				if (emoji.time + timeout < start) {
					emoji.dying = true;
				}

				ctx.globalAlpha = emoji.opacity;

				if (emoji.content instanceof Image) {
					ctx.drawImage(emoji.content, emoji.x, emoji.y, relativeSize, relativeSize);
				} else {
					ctx.fillText(emoji.content, emoji.x, emoji.y);
				}

				if (emoji.dying) {
					if (emoji.opacity > 0.005) {
						emoji.opacity -= 0.01;

						if (emoji.opacity < 0) {
							emoji.opacity = 0;
						}
					} else {
						emojis.splice(emojis.indexOf(emoji), 1);
					}
				} else if (emoji.opacity < 1) {
					emoji.opacity += 0.01;
				}

				if (emoji.y > canvas.height + relativeSize) {
					emoji.x = Math.random() * canvas.width;
					emoji.y = -relativeSize;
				}

				ctx.restore();
			});
		}

		const time = performance.now() - start;
		avgTickTime = (avgTickTime + time) / 2;

		requestAnimationFrame(draw);
	}

	onMount(() => {
		Widget.on('update', () => {
			timeout = Widget.getSetting('rain_settings.life_time') * 1000;
			maxEmojis = Widget.getSetting('rain_settings.max_emojis');
			size = Widget.getSetting('rain_settings.size');
			speed = Widget.getSetting('rain_settings.speed') / 25;

			ctx.font = size + 'px Sans-Serif';
		});

		Koi.on('RICH_MESSAGE', (event) => {
			for (const fragment of event.fragments) {
				console.debug('Fragment:', fragment);
				switch (fragment.type) {
					case 'EMOTE': {
						create(fragment.imageLink);
						break;
					}
					case 'EMOJI': {
						const asset = fragment.variation.assets[App.get('emojiProvider')];
						if (asset) {
							create(asset.pngUrl);
						} else {
							create(fragment.raw);
						}
						break;
					}
				}
			}
		});

		// setInterval(() => {
		// 	console.debug(
		// 		avgTickTime.toFixed(2) +
		// 			'ms/frame ' +
		// 			emojis.length +
		// 			' emojis (' +
		// 			((avgTickTime / frameTime) * 100).toFixed(2) +
		// 			'% of the frame budget)'
		// 	);
		// }, 10000);

		canvas.width = window.innerWidth;
		canvas.height = window.innerHeight;

		ctx = canvas.getContext('2d');
		Widget.broadcast('update');

		requestAnimationFrame(draw);
	});
</script>

<canvas bind:this={canvas} class="w-full h-full" />

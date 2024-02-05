<script>
	import { onMount } from 'svelte';

	let timeout = 10 * 1000;
	let maxEmojis = 1000;
	let size = 15;
	let speed = 1;
	let container;

	const minOpacity = 0.8;
	let avgTickTime = 0;
	let emojis = [];

	const targetDelta = 1000 / 60; // 60hz
	let start = performance.now();

	function create(content) {
		if (emojis.length < maxEmojis) {
			const scale = Math.random() * (1 - minOpacity) + minOpacity;

			if (content.startsWith('http') || content.startsWith('data:')) {
				const image = new Image();
				image.src = content;
				content = image;
			} else {
				const span = document.createElement('span');
				span.innerText = content;
				content = span;
			}

			content.style.position = 'absolute';

			const emoji = {
				x: Math.random() * container.offsetWidth,
				y: Math.random() * container.offsetHeight,
				ys: Math.random() + 2,
				scale: scale,
				opacity: 0.01,
				content: content,
				dying: false,
				time: performance.now()
			};

			container.append(content);

			emojis.push(emoji);
		}
	}

	function draw() {
		const delta = performance.now() - start;
		const deltaRate = delta / targetDelta;

		start += delta;

		for (const emoji of emojis) {
			emoji.y += emoji.ys * deltaRate * speed;

			const relativeSize = size * emoji.scale;

			if (emoji.time + timeout < start) {
				emoji.dying = true;
			}

			emoji.content.style.opacity = emoji.opacity;
			emoji.content.style.width = relativeSize + 'px';
			emoji.content.style.top = emoji.y + 'px';
			emoji.content.style.left = emoji.x + 'px';

			if (emoji.dying) {
				if (emoji.opacity > 0.005) {
					emoji.opacity -= 0.01;

					if (emoji.opacity < 0) {
						emoji.opacity = 0;
					}
				} else {
					emoji.content.remove();
					emojis.splice(emojis.indexOf(emoji), 1);
				}
			} else if (emoji.opacity < 1) {
				emoji.opacity += 0.01;
			}

			if (emoji.y > container.offsetHeight + relativeSize) {
				emoji.x = Math.random() * container.offsetWidth;
				emoji.y = -relativeSize;
			}
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

			container.style.font = size + 'px';
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

		window.test = () => {
			create('https://static-cdn.jtvnw.net/emoticons/v2/305954156/default/dark/3.0');
		};

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

		Widget.broadcast('update');
		requestAnimationFrame(draw);
	});
</script>

<div bind:this={container} class="relative w-full h-full" />

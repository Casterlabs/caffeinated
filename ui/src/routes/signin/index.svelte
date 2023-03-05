<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { onDestroy } from 'svelte';

	const PLATFORMS = [
		{
			id: 'CAFFEINE',
			name: 'Caffeine',
			color: '#0000ff'
		},
		{
			id: 'TWITCH',
			name: 'Twitch',
			color: '#8838ff'
		},
		{
			id: 'TROVO',
			name: 'Trovo',
			color: '#149b53'
		},
		{
			id: 'GLIMESH',
			name: 'Glimesh',
			color: '#33528b'
		},
		// {
		// 	id: 'BRIME',
		// 	name: 'Brime',
		// 	color: 'linear-gradient(45deg, #8439af 15%, #fc3537 65%)'
		// },
		{
			id: 'YOUTUBE',
			name: 'YouTube (BETA)',
			color: '#ff0000'
		},
		{
			id: 'DLIVE',
			name: 'DLive',
			color: '#ccc121'
		},
		{
			id: 'THETA',
			name: 'Theta',
			color: '#161a24'
		}
	];

	// DOOT!
	const ACTIVATE_AT = 5;
	let dootCount = 0; // -1 = Lock, -2 = End.
	let doots = [];

	function doDoot() {
		if (dootCount < ACTIVATE_AT) {
			dootCount++;

			const audio = new Audio('/doots/1.mp3');
			audio.volume = 0.1;
			audio.play();
			return;
		}

		const dootImg = new Image();
		dootImg.src = '/doots/doot.png';

		const dootCanvas = document.querySelector('#doot-canvas');
		const dootCtx = dootCanvas.getContext('2d');

		function dootRenderLoop() {
			const start = performance.now();

			const width = window.innerWidth;
			const height = window.innerHeight;

			dootCanvas.width = width;
			dootCanvas.height = height;

			if (doots.length > 0) {
				dootCtx.clearRect(0, 0, width, height);

				for (const dootItem of doots) {
					const dootSize = dootItem.scale * 36;
					dootItem.y += dootItem.ys / height;

					if (dootItem.y > dootSize / height + 1) {
						dootItem.x = Math.random();
						dootItem.y = 0;
					}

					dootCtx.drawImage(
						dootImg,
						dootItem.x * width,
						dootItem.y * height - dootSize,
						dootSize,
						dootSize
					);
				}
			}

			const time = performance.now() - start;

			if (dootCount == -1) {
				setTimeout(() => requestAnimationFrame(dootRenderLoop), 1000 / 60 - time);
			}
		}

		// Lock the UI.
		dootCanvas.style.pointerEvents = 'all';
		dootCount = -1;

		// Add a bunch of doots.
		for (let i = 0; i != 200; i++) {
			const scale = Math.random() * (1.6 - 0.4) + 0.4;
			doots.push({
				x: Math.random(),
				y: Math.random(),
				ys: Math.random() * 2 + 1,
				scale: scale
			});
		}

		const audio = new Audio('/doots/2.mp3');
		audio.volume = 0.1;

		// Show the canvas after the drop.
		audio.addEventListener('play', () => {
			setTimeout(() => {
				dootCanvas.style.opacity = 1;
			}, 17680);
		});

		// Hide the canvas after the audio stops, then remove the doots and reset the counter.
		audio.addEventListener('ended', () => {
			dootCanvas.style.opacity = 0;
			dootCanvas.style.pointerEvents = 'none';

			setTimeout(() => {
				doots.length = 0;
				dootCount = 0;
			}, 300);
		});

		// DO THE DOOT!
		audio.play();
		requestAnimationFrame(dootRenderLoop);
	}

	onDestroy(() => {
		dootCount = -2;
	});
</script>

<div class="h-full w-fit mx-auto pt-10 relative">
	<h1 class="font-medium text-2xl text-center">
		<LocalizedText key="page.signin.welcome" />
	</h1>
	<h2 class="text-sm text-base-11 mt-1 text-center">
		<LocalizedText key="page.signin.choose" />
	</h2>

	<div class="mt-4 w-fit mx-auto grid grid-cols-4 gap-2">
		{#each PLATFORMS as { id, name, color }}
			<a
				href="/signin/{id.toLowerCase()}?dontGoBack"
				class="signin-icon w-12 h-12 border border-base-6 rounded inline-flex items-center justify-center transition"
				style="--color: {color};"
				title={name}
			>
				<icon data-icon="service/{id.toLowerCase()}" />
			</a>
		{/each}
	</div>

	<span class="absolute inset-x-0 bottom-2 text-xs text-base-11">
		<LocalizedText
			key="page.signin.disclaimer"
			slotMapping={['terms_of_service', 'privacy_policy', 'here']}
		>
			<a slot="0" class="text-nqp" href="https://casterlabs.co/terms-of-service" target="_blank">
				<LocalizedText key="page.signin.disclaimer.terms_of_service" />
			</a>
			<a slot="1" class="text-nqp" href="https://casterlabs.com/privacy-policy" target="_blank">
				<LocalizedText key="page.signin.disclaimer.privacy_policy" />
			</a>

			<button slot="2" class="text-nqp" on:click={doDoot}>
				<LocalizedText key="page.signin.disclaimer.here" />
			</button>
		</LocalizedText>
	</span>
</div>

<style>
	.signin-icon:hover {
		background: var(--color);
		color: white;
	}
</style>

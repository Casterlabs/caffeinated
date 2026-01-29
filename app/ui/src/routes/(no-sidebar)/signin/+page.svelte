<script lang="ts">
	import { modify, storify } from '$lib/bridge-helper';
	import { PLATFORM_COLORS, type UserPlatform } from '$lib/koi';

	import PlatformIcon from '$lib/layout/PlatformIcon.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { Select } from '@casterlabs/ui';

	import { onDestroy } from 'svelte';

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();

	// DOOT!
	const ACTIVATE_AT = 5;
	let dootCount = 0; // -1 = Lock, -2 = End.
	let doots: { x: number; y: number; ys: number; scale: number }[] = [];

	function doDoot() {
		if (dootCount < ACTIVATE_AT) {
			dootCount++;

			const audio = new Audio('/$caffeinated-sdk-root$/doots/1.mp3');
			audio.volume = 0.1;
			audio.play();
			return;
		}

		const dootImg = new Image();
		dootImg.src = '/$caffeinated-sdk-root$/doots/doot.png';

		const dootCanvas = document.querySelector('#doot-canvas') as HTMLCanvasElement;
		const dootCtx = dootCanvas.getContext('2d')!;

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

					dootCtx.drawImage(dootImg, dootItem.x * width, dootItem.y * height - dootSize, dootSize, dootSize);
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

		const audio = new Audio('/$caffeinated-sdk-root$/doots/2.mp3');
		audio.volume = 0.1;

		// Show the canvas after the drop.
		audio.addEventListener('play', () => {
			setTimeout(() => {
				dootCanvas.style.opacity = '1';
			}, 17680);
		});

		// Hide the canvas after the audio stops, then remove the doots and reset the counter.
		audio.addEventListener('ended', () => {
			dootCanvas.style.opacity = '0';
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
		<LocalizedText key="co.casterlabs.caffeinated.app.page.signin.welcome" />
	</h1>
	<h2 class="text-sm text-base-11 mt-1 text-center">
		<LocalizedText key="co.casterlabs.caffeinated.app.page.signin.choose" />
	</h2>

	<div class="mt-8 w-64 mx-auto flex flex-wrap justify-center">
		<!-- Loop over all of the "official" platforms -->
		{#await AppAuth.AUTHENTICATABLE then AUTHENTICATABLE_PLATFORMS}
			{#each AUTHENTICATABLE_PLATFORMS as platform}
				<a
					href="/$caffeinated-sdk-root$/signin/do?dontGoBack&type=koi&platform={platform}"
					class="signin-icon w-10 h-10 p-1 m-1 border border-base-6 rounded flex items-center justify-center transition"
					style="--color: {PLATFORM_COLORS[platform as UserPlatform]};"
				>
					<PlatformIcon class="text-lg" {platform} />

					<span class="sr-only">
						<LocalizedText key="co.casterlabs.caffeinated.app.platform.{platform}" />
					</span>
				</a>
			{/each}
		{/await}
	</div>

	<div class="absolute inset-x-0 bottom-2">
		{#await AppLocale.available then locales}
			<label>
				<p class="text-sm font-medium text-base-11">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.appearance.language" />
				</p>

				<div class="mt-2">
					<Select
						class="w-full"
						onchange={(e) => {
							const value = parseFloat((e.target as HTMLSelectElement).value);
							modify(AppConfig, 'uiPreferences', 'language', value);
						}}
					>
						{#each Object.entries(locales) as [k, v]}
							<option value={k} selected={k === $uiPreferences?.language}>{v}</option>
						{/each}
					</Select>
				</div>
			</label>
		{/await}

		<p class="mt-6 text-xs text-base-11">
			{#snippet terms_of_service()}
				<a class="text-primary-11" href="https://casterlabs.co/terms-of-service" target="_blank">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.signin.disclaimer.terms_of_service" />
				</a>
			{/snippet}

			{#snippet privacy_policy()}
				<a class="text-primary-11" href="https://casterlabs.co/privacy-policy" target="_blank">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.signin.disclaimer.privacy_policy" />
				</a>
			{/snippet}

			{#snippet here()}
				<button class="text-primary-11" onclick={doDoot}>
					<LocalizedText key="co.casterlabs.caffeinated.app.page.signin.disclaimer.here" />
				</button>
			{/snippet}

			<LocalizedText key="co.casterlabs.caffeinated.app.page.signin.disclaimer" components={{ terms_of_service, privacy_policy, here }} />
		</p>
	</div>
</div>

<style>
	.signin-icon:hover {
		background: var(--color);
		color: white;
	}
</style>

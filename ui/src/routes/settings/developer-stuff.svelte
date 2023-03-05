<script>
	import { goto } from '$app/navigation';
	import SelectMenu from '$lib/ui/SelectMenu.svelte';

	const PLATFORMS = [
		'',
		'CAFFEINE',
		'TWITCH',
		'TROVO',
		'GLIMESH',
		'BRIME',
		'YOUTUBE',
		'DLIVE',
		'TIKTOK',
		'THETA',
		'KICK'
	];

	const SPECIAL_PLATFORMS = {
		CAFFEINE: '/signin/caffeine',
		KICK: '/signin/kick'
	};

	let manualAuthPlatform = 0;
</script>

<SelectMenu
	title="Manually authenticate platform"
	options={PLATFORMS}
	bind:value={manualAuthPlatform}
	on:value={({ detail: index }) => {
		if (index == 0) return;
		manualAuthPlatform = 0;

		const platform = PLATFORMS[index];
		if (SPECIAL_PLATFORMS[platform]) {
			goto(SPECIAL_PLATFORMS[platform]);
		} else {
			goto(`/signin/oauth?type=koi&platform=${platform.toLowerCase()}`);
		}
	}}
/>

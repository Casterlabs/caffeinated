<script lang="ts">
	import { HSL, adjustTextColor } from '$lib/contrast-ratio';
	import { PLATFORM_COLORS, type User } from '$lib/koi';

	import PlatformIcon from '../PlatformIcon.svelte';

	const DEFAULT_AVATAR =
		'data:image/svg+xml,%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22UTF-8%22%20standalone%3D%22no%22%3F%3E%0A%3Csvg%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22whitesmoke%22%20stroke-width%3D%221.5%22%20version%3D%221.1%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20xmlns%3Asvg%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%20%20%20%20%3Crect%20style%3D%22fill%3A%23676767%3Bfill-opacity%3A1%3Bstroke%3Anone%3B%22%20width%3D%2224%22%20height%3D%2224%22%20x%3D%220%22%20y%3D%220%22%20%2F%3E%0A%20%20%20%20%3Cpath%20d%3D%22M20%2021v-2a4%204%200%200%200-4-4H8a4%204%200%200%200-4%204v2%22%20%2F%3E%0A%20%20%20%20%3Ccircle%20cx%3D%2212%22%20cy%3D%227%22%20r%3D%224%22%20%2F%3E%0A%3C%2Fsvg%3E%0A';

	interface Props {
		user: User;
		showBadges?: boolean;
	}

	let { user, showBadges = true }: Props = $props();

	let contrastColor = $derived(
		(() => {
			try {
				const MIN_RATIO = 8; // Arbitrary. Needs to be high enough to remain readable even with abhorrent colors like #0000ff

				const pageBackground = getComputedStyle(document.getElementById('css-intermediate')!).getPropertyValue('--base1');

				const usernameColor = HSL.from(user.color);
				const backgroundColor = HSL.from(pageBackground);

				const contrastColor = adjustTextColor(backgroundColor, usernameColor, MIN_RATIO);

				return contrastColor.toCss();
			} catch (e) {
				console.warn('An error occurred whilst calculating contrast color, defaulting to nqp.', e);
				return 'var(--primary11)';
			}
		})()
	);

	let usernameDiffersFromDisplayname = $derived(user.username.toLowerCase() != user.displayname.toLowerCase());
</script>

<span class="username">
	<span class="space-x-0.5">
		<PlatformIcon class="er-platform inline-block h-[1.2em] w-[1.2em]" platform={user.platform} color />

		<img
			class="er-profile-picture inline-block rounded-full h-[1.2em] w-[1.2em] object-cover"
			alt=""
			src={user.image_link}
			onerror={(e) => {
				// @ts-ignore
				e.target.src = DEFAULT_AVATAR;
			}}
		/>

		{#if showBadges}
			{#each user.badges as badge}
				<img class="er-badges inline-block h-[1.2em] w-[1.2em]" alt="" src={badge} />
			{/each}
		{/if}
	</span>

	<span class="er-name font-semibold" style:--user-color={user.color} style:--contrast-color={contrastColor} style:--platform-color={PLATFORM_COLORS[user.platform]}>
		{user.displayname}{#if usernameDiffersFromDisplayname}<span class="text-xs"> ({user.username})</span>{/if}</span
	>:
</span>

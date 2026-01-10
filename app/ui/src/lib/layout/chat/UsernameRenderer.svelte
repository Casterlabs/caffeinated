<script lang="ts">
	import { HSL, adjustTextColor } from '$lib/contrast-ratio';
	import { PLATFORM_COLORS, type User } from '$lib/koi';

	import PlatformIcon from '../PlatformIcon.svelte';

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

		<img class="er-profile-picture inline-block rounded-full h-[1.2em] w-[1.2em] object-cover" alt="" src={user.image_link} />

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

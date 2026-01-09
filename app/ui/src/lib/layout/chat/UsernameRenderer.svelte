<script lang="ts">
	import { HSL, adjustTextColor } from '$lib/contrast-ratio';
	import type { User } from '$lib/koi';

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
	<img class="er-profile-picture inline-block rounded-full h-[20px] w-[20px] object-cover" alt="" src={user.image_link} />

	<span class="er-platform inline-block h-[20px] w-[20px]">
		<PlatformIcon platform={user.platform} color />
	</span>

	{#if showBadges && user.badges.length > 0}
		<span class="er-badges space-x-1" aria-hidden="true">
			{#each user.badges as badge}
				<img class="inline-block h-[1em] w-[1em] -translate-y-0.5 align-middle" alt="" src={badge} />
			{/each}
		</span>
	{/if}

	<span class="name" style:--user-color={user.color} style:--contrast-color={contrastColor}>
		{user.displayname}{#if usernameDiffersFromDisplayname}<span class="text-xs"> ({user.username})</span>{/if}</span
	>:
</span>

<style>
	.username > .name {
		color: var(--contrast-color);
		/* color: var(--user-color); */
		/* color: var(--color-accent-9); */
		font-weight: 600;
	}
</style>

<script lang="ts">
	import type { RaidEvent } from '$lib/koi';

	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: RaidEvent;
	}

	let { event }: Props = $props();
</script>

<UsernameRenderer user={event.host} />

{#if event.viewers > 1}
	is raiding with <b>{event.viewers}</b> viewers!
{:else}
	has raided the channel!
{/if}

<style>
	b {
		color: var(--color-accent-9);
		font-weight: 600;
	}
</style>

<script lang="ts">
	import type { FollowEvent } from '$lib/koi';

	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: FollowEvent;
	}

	let { event }: Props = $props();
</script>

<UsernameRenderer user={event.follower} /> just followed

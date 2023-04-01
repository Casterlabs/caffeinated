<svelte:options accessors />

<script>
	import { fade } from 'svelte/transition';
	import { onMount } from 'svelte';

	export let settings; // svelte store
	export let event;
	export let offsetX = '0%';

	let visible = true;

	let disappearTask = -1;
	function checkDisappear() {
		clearTimeout(disappearTask);

		if (!$settings['message_style.disappearing']) {
			visible = true;
			return;
		}

		const timeSinceAdded = Date.now() - new Date(event.timestamp);
		const disappearAfter = $settings['message_style.disappear_after'] * 1000;
		const disappearIn = disappearAfter - timeSinceAdded;

		if (disappearIn > 0) {
			visible = true;
			disappearTask = setTimeout(() => {
				visible = false;
			}, disappearIn);
		} else {
			visible = false;
		}
	}
	onMount(() => settings.subscribe(checkDisappear));
</script>

{#if visible}
	<div
		id="slot"
		style:--x-offset={offsetX}
		in:fade={{ duration: 200 }}
		out:fade={{ duration: 750 }}
	/>
{/if}

<style>
	div {
		transform: translate(var(--x-offset), var(--item-offset));
	}
</style>

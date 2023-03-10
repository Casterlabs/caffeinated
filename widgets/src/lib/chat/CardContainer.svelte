<svelte:options accessors />

<script>
	export let settings; // svelte store
	export let offsetX = 0;
</script>

<div id="slot" style:--x-offset={offsetX} />

<style>
	div {
		transform: translateX(var(--x-offset));
	}
</style>

<script>
	export let aspectRatio = 9 / 16;
</script>

<div class="aspect-container" style:--aspect={aspectRatio}>
	<div>
		<slot />
	</div>
</div>

<style>
	.aspect-container {
		position: relative;
		padding-bottom: calc(var(--aspect) * 100%);
		overflow: hidden;
	}
	.aspect-container > div {
		position: absolute;
		top: 0;
		left: 0;
		height: 100%;
		width: 100%;
	}
</style>

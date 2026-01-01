<div class="aspect-square">
	<span class="loader"></span>
</div>

<style>
	.loader {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		display: inline-block;
		border-top: 3px solid var(--base8);
		border-right: 3px solid transparent;
		margin-bottom: 3px;
		box-sizing: border-box;
		animation: rotation 1s linear infinite;
	}

	@keyframes rotation {
		0% {
			transform: rotate(0deg);
		}

		100% {
			transform: rotate(360deg);
		}
	}
</style>

<icon class="hint-arrow absolute top-12 right-4" data-icon="icon/arrow-small-up" />

<div class="w-full h-full flex justify-center items-center text-center">
	<div>
		<h1 class="text-xl font-semibold">Welcome!</h1>
		<p>Get started by unlocking your dashboard</p>
	</div>
</div>

<style>
	.hint-arrow {
		animation-name: hintAnimation;
		animation-duration: 1.5s;
		animation-iteration-count: infinite;
		animation-play-state: running;
	}

	@keyframes hintAnimation {
		0% {
			transform: translateY(0);
			animation-timing-function: ease-in-out;
		}

		50% {
			transform: translateY(-0.5rem);
			animation-timing-function: ease-in-out;
		}

		100% {
			transform: translateY(0);
			animation-timing-function: ease-in-out;
		}
	}
</style>

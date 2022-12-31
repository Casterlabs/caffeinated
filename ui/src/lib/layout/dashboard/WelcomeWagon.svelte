<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import ResizableGrid from '../ResizableGrid.svelte';

	/** @type {ResizableGrid} */
	export let grid;

	$: resizingLocked = grid?.isResizingLocked;
</script>

{#if $resizingLocked}
	<div class="w-full h-full flex justify-center items-center text-center">
		<div>
			<h1 class="text-xl font-semibold">
				<LocalizedText key="dashboard.customize.welcomewagon.welcome" />
			</h1>
			<p>
				<LocalizedText key="dashboard.customize.welcomewagon.getstarted" />
			</p>
		</div>
	</div>

	<icon class="hint-arrow fixed top-12 right-6" data-icon="icon/arrow-small-up" />
{:else}
	<icon
		class="hint-arrow absolute top-11 right-1/2 -translate-x-1/2"
		data-icon="icon/arrow-small-up"
	/>

	<p class="text-center pt-16">
		<LocalizedText key="dashboard.customize.welcomewagon.next" />
	</p>
{/if}

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

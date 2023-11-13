<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import ResizableGrid from '$lib/layout/ResizableGrid.svelte';

	/** @type {ResizableGrid} */
	export let grid;

	$: resizingLocked = grid?.isResizingLocked;
</script>

{#if $resizingLocked}
	<div class="w-full h-full flex justify-center items-center text-center">
		<div>
			<h1 class="text-xl font-semibold">
				<LocalizedText
					key="co.casterlabs.caffeinated.app.page.dashboard.customize.welcomewagon.welcome"
				/>
			</h1>
			<p>
				<LocalizedText
					key="co.casterlabs.caffeinated.app.page.dashboard.customize.welcomewagon.getstarted"
				/>
			</p>
		</div>
	</div>

	<icon class="hint-arrow fixed top-12 right-6" data-icon="icon/arrow-small-up" />
{:else}
	<icon
		class="hint-arrow absolute top-14 right-1/2 -translate-x-1/2"
		data-icon="icon/arrow-small-up"
	/>

	<div class="text-center pt-20 -translate-y-2">
		<h1 class="font-semibold text-md">
			<LocalizedText
				key="co.casterlabs.caffeinated.app.page.dashboard.customize.welcomewagon.next"
			/>
		</h1>
		<h2 class="font-thin text-sm">
			<LocalizedText
				key="co.casterlabs.caffeinated.app.page.dashboard.customize.welcomewagon.also"
			/>
		</h2>
	</div>
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

<script>
	import LocalizedText from '../../LocalizedText.svelte';

	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	export let icon = null;
	export let text = null;
</script>

<button
	class="relative flex items-center space-x-3 rounded-lg border border-mauve-6 bg-mauve-2 p-5 shadow-sm focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7"
	on:click={() => dispatch('click')}
>
	<div class="flex-shrink-0 text-mauve-12">
		<icon data-icon={icon} />
	</div>
	<div class="min-w-0 flex-1">
		<p class="text-sm font-medium text-mauve-12 text-left">
			<LocalizedText key={text} />
		</p>
	</div>
</button>

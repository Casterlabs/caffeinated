<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { createEventDispatcher } from 'svelte';
	import { goto } from '$app/navigation';

	const dispatch = createEventDispatcher();

	export let icon = null;
	export let text = null;
	export let title = null;

	export let href = null;

	function onClick() {
		if (href) {
			goto(href);
		} else {
			dispatch('click');
		}
	}
</script>

<button
	role={href ? 'link' : 'button'}
	class="rounded-lg border border-base-6 bg-base-2 p-5 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
	on:click={onClick}
	{title}
>
	<div class="w-full relative flex items-center text-base-12 space-x-3">
		<div class="flex-0">
			<icon data-icon="icon/{icon}" />
		</div>
		<div
			class="flex-1 min-width-[0px] whitespace-nowrap text-ellipsis overflow-hidden text-sm font-medium text-left"
		>
			<LocalizedText key={text} />
		</div>
		<div class="flex-0">
			<slot />
		</div>
	</div>
</button>

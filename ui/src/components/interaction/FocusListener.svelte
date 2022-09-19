<script>
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	export let style = '';
	let className = '';
	export { className as class };

	export let ignoreFocusState = false;
	export let focused = false;
	let div;

	function isDescendant(parent, child) {
		if (child == null) {
			return false;
		}

		let node = child;
		while (node != null) {
			if (node == parent) {
				return true;
			}
			node = node.parentNode;
		}

		return false;
	}

	function lostFocus(e) {
		if (!focused && !ignoreFocusState) return;
		const target = e.relatedTarget;

		if (target?.hasAttribute('focus-ignore')) {
			return;
		}

		// If the new focused element is NOT one of ours.
		if (!isDescendant(div, target)) {
			focused = false;
			dispatch('lostfocus');
		}
	}

	function gainedFocus(e) {
		if (focused && !ignoreFocusState) return;
		focused = true;
		dispatch('gainedfocus');
	}
</script>

<div
	class={className}
	{style}
	bind:this={div}
	on:focusin={gainedFocus}
	on:focusout={lostFocus}
	on:click={gainedFocus}
>
	<slot />
</div>

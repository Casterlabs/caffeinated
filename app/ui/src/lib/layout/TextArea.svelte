<script lang="ts" module>
	import type { HTMLTextareaAttributes } from 'svelte/elements';

	interface Props extends HTMLTextareaAttributes {
		/**
		 * Removes the borders from the element. This has the side effect of also making the element transparent and also removes the outline when focused/active.
		 */
		borderless?: boolean;
		value?: any;
	}
</script>

<script lang="ts">
	let {
		borderless,
		value = $bindable(),

		...props
	}: Props = $props();

	let inputElement: HTMLTextAreaElement = $state({} as HTMLTextAreaElement);
</script>

<textarea bind:this={inputElement} class:borderless bind:value {...props}></textarea>

<style>
	/* Generic */

	textarea {
		border-radius: var(--clui-radius, 0);
		border-width: 0.0625rem;
		border-style: solid;
		border-color: transparent;
		background-color: transparent;
		color: currentColor;
		font-size: 0.8rem;
	}

	textarea[disabled] {
		cursor: not-allowed;
	}

	textarea[readonly] {
		caret-color: transparent;
	}

	textarea:not(.borderless) {
		border-color: var(--clui-color-base-7);
		background-color: var(--clui-color-base-3);
		color: var(--clui-color-base-12);
		padding: var(--clui-padding);
	}

	textarea:not(.borderless)::placeholder {
		color: var(--clui-color-base-11);
	}

	/* Generic Hover */

	textarea:not(.borderless):not([readonly]):not([disabled]):hover {
		border-color: var(--clui-color-base-8);
	}

	/* Generic Focus */

	textarea:focus {
		outline: none;
	}

	textarea:not(.borderless):not([readonly]):not([type='range']):focus {
		outline-width: 0.125rem;
		outline-style: solid;
		outline-color: var(--clui-color-accent-8);
		border-color: var(--clui-color-base-2);
	}
</style>

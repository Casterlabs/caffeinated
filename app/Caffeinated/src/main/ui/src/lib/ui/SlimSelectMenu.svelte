<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import FocusListener from '$lib/interaction/FocusListener.svelte';

	import { createEventDispatcher } from 'svelte';
	import { fade } from 'svelte/transition';
	import { t } from '$lib/app.mjs';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let value = '';
	export let options = {};
	export let disabled = false;
	export let width = 'fit';

	let open = false;
	let highlighted = null;

	$: open, open && (highlighted = value);

	function select(id) {
		value = id;
		open = false;
		dispatch('value', id);
	}
</script>

<div
	class="inline-block w-{width}"
	class:opacity-70={disabled}
	class:pointer-events-none={disabled}
>
	<FocusListener class="relative" on:lostfocus={() => (open = false)}>
		<!-- svelte-ignore a11y-no-static-element-interactions -->
		<div
			on:keyup={(e) => {
				if (e.code == 'Enter') {
					// Handle the enter key if the user has a highlighted option.
					if (highlighted) {
						select(highlighted);
					}
				} else if (e.code == 'ArrowDown' || e.code == 'ArrowUp') {
					// ArrowDown: 1, ArrowUp: -1.
					// 1: Descending, -1: Ascending.
					const direction = e.code == 'ArrowUp' ? -1 : 1;
					const keys = Object.keys(options);

					// If we have a highlighted option, then we want to move based on that.
					if (highlighted) {
						let curr = keys.indexOf(highlighted);
						curr += direction;

						// Wrap around.
						if (curr < 0) {
							highlighted = keys[keys.length - 1];
						} else if (curr >= keys.length) {
							highlighted = keys[0];
						}
						// Navigate to the next item.
						else {
							highlighted = keys[curr];
						}
					} else {
						// Otherwise, snap to the top or bottom of the list.
						if (direction == 1) {
							highlighted = keys[0];
						} else {
							highlighted = keys[keys.length - 1];
						}
					}
				}
			}}
		>
			<!-- svelte-ignore a11y-role-supports-aria-props -->
			<button
				type="button"
				role="listbox"
				class="align-middle relative w-{width} h-fit cursor-pointer rounded-md border border-base-7 bg-base-1 py-1 pl-1.5 pr-5 text-left shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
				aria-haspopup="listbox"
				aria-expanded={open}
				aria-labelledby={ID}
				aria-disabled={disabled}
				on:click={() => (open = !open)}
			>
				<LocalizedProperty key={options[value]} property="title" />
				<!-- Tricks the CSS renderer into giving us the correct width -->
				<!-- We need to be sure this is hidden from screen readers, though. -->
				<div aria-hidden="true" class="h-0 overflow-hidden">
					{#each Object.values(options) as name}
						<span class="block">
							<LocalizedText key={name} />
						</span>
					{/each}
				</div>

				<span class="block truncate text-base-12">
					<LocalizedText key={options[value]} />&nbsp;
				</span>
				<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-0.5">
					<icon class="h-4 w-4 text-base-10" data-icon="icon/chevron-up-down" />
				</span>
			</button>

			{#if open}
				<div class="absolute z-10 mt-1 w-full shadow-lg rounded-md bg-base-1">
					<ul
						class="overflow-auto max-h-36 rounded-md py-1 ring-1 ring-base-8 ring-opacity-5 focus:outline-none text-sm"
						role="listbox"
						tabindex="-1"
						aria-activedescendant="{ID}_{value}"
						transition:fade|local={{ duration: 75 }}
					>
						{#each Object.entries(options) as [id, name]}
							{@const isSelected = value == id}
							{@const isHighlighted = highlighted == id}

							<li
								id="{ID}_{value}"
								class="font-normal relative cursor-default select-none"
								class:text-base-12={!isHighlighted}
								class:bg-primary-9={isHighlighted}
								class:text-white={isHighlighted}
								role="option"
								aria-selected={isSelected}
								on:mouseleave={() => {
									if (isHighlighted) {
										highlighted = null;
									}
								}}
								on:mouseenter={() => (highlighted = id)}
							>
								<button class="w-full text-left py-2 pl-3 pr-9" on:click={() => select(id)}>
									<LocalizedProperty key={name} property="title" />
									<span class="block truncate" class:font-semibold={isSelected}>
										<LocalizedText key={name} />
									</span>

									{#if isSelected}
										<span
											class="absolute inset-y-0 right-0 flex items-center pr-4"
											class:text-primary-9={!isHighlighted}
											class:text-white={isHighlighted}
										>
											<icon class="h-5 w-5" data-icon="icon/check" />
										</span>
									{/if}
								</button>
							</li>
						{/each}
					</ul>
				</div>
			{/if}
		</div>
	</FocusListener>
</div>

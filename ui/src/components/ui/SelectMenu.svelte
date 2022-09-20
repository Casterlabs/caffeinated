<script>
	import LocalizedText from '../LocalizedText.svelte';
	import FocusListener from '../interaction/FocusListener.svelte';

	import { createEventDispatcher } from 'svelte';
	import { fade } from 'svelte/transition';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let title = 'Assigned to';
	export let value = 'tim-cook';
	export let options = {
		'tim-cook': 'Tim Cook',
		'bill-gates': 'Bill Gates'
	};

	let open = false;
	let highlighted = null;

	$: open, open && (highlighted = value);

	function select(id) {
		value = id;
		open = false;
		dispatch('value', id);
	}
</script>

<div class="w-full">
	<label class="block text-sm font-medium text-mauve-12" for={ID}>
		<LocalizedText key={title} />
	</label>

	<FocusListener class="relative mt-1" on:lostfocus={() => (open = false)}>
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
			<button
				id={ID}
				type="button"
				role="listbox"
				class="relative w-full h-[2.375rem] cursor-pointer rounded-md border border-mauve-7 bg-mauve-1 py-2 pl-3 pr-10 text-left shadow-sm focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7 text-sm"
				aria-haspopup="listbox"
				aria-expanded={open}
				on:click={() => (open = !open)}
			>
				<span class="block truncate text-mauve-12">
					<LocalizedText key={options[value]} />
				</span>
				<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
					<icon class="h-5 w-5 text-mauve-10" data-icon="chevron-up-down" />
				</span>
			</button>

			{#if open}
				<div class="absolute z-10 mt-1 max-h-60 w-full shadow-lg rounded-md bg-mauve-1">
					<ul
						class="overflow-auto rounded-md py-1 ring-1 ring-mauve-8 ring-opacity-5 focus:outline-none text-sm"
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
								class:text-mauve-12={!isHighlighted}
								class:bg-crimson-9={isHighlighted}
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
									<span class="block truncate" class:font-semibold={isSelected}>
										<LocalizedText key={name} />
									</span>

									{#if isSelected}
										<span
											class="absolute inset-y-0 right-0 flex items-center pr-4"
											class:text-crimson-9={!isHighlighted}
											class:text-white={isHighlighted}
										>
											<icon class="h-5 w-5" data-icon="check" />
										</span>
									{/if}</button
								>
							</li>
						{/each}
					</ul>
				</div>
			{/if}
		</div>
	</FocusListener>
</div>

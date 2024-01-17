<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import FocusListener from '$lib/interaction/FocusListener.svelte';
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';

	import { createEventDispatcher } from 'svelte';
	import { fade } from 'svelte/transition';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let value = '';
	export let disabled = false;
	export let width = 'fit';

	let open = false;
	let highlighted = null;

	$: open, open && (highlighted = value);

	export let search = async () => ['test'];
	let options = ['Tim Cook', 'Bill Gates'];
	let loading = false;

	$: value,
		(async () => {
			try {
				loading = true;
				options = await search(value);
			} finally {
				loading = false;
			}
		})();

	function select(name) {
		value = name;
		open = false;
		dispatch('value', name);
	}
</script>

<div
	class="inline-block w-{width}"
	class:opacity-70={disabled}
	class:pointer-events-none={disabled}
>
	<FocusListener class="relative mt-1" on:lostfocus={() => (open = false)}>
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

					// If we have a highlighted option, then we want to move based on that.
					if (highlighted) {
						let curr = options.indexOf(highlighted);
						curr += direction;

						// Wrap around.
						if (curr < 0) {
							highlighted = options[options.length - 1];
						} else if (curr >= options.length) {
							highlighted = options[0];
						}
						// Navigate to the next item.
						else {
							highlighted = options[curr];
						}
					} else {
						// Otherwise, snap to the top or bottom of the list.
						if (direction == 1) {
							highlighted = options[0];
						} else {
							highlighted = options[options.length - 1];
						}
					}
				}
			}}
		>
			<!-- svelte-ignore a11y-role-supports-aria-props -->
			<div
				class="inline-block relative w-{width} h-fit"
				role="listbox"
				aria-haspopup="listbox"
				aria-expanded={open}
				aria-labelledby={ID}
			>
				<input
					type="input"
					class="w-full h-fit cursor-pointer rounded-md border border-base-7 bg-base-1 py-1 pl-1.5 pr-5 text-left shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
					title={value}
					{disabled}
					bind:value
					on:click={() => (open = true)}
				/>
				<!-- Tricks the CSS renderer into giving us the correct width -->
				<!-- We need to be sure this is hidden from screen readers, though. -->
				<div aria-hidden="true" class="h-0 overflow-hidden">
					{#each options as name}
						<span class="block">
							<LocalizedText key={name} />
						</span>
					{/each}
				</div>

				{#if loading}
					<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-1">
						<div class="w-5 scale-[.7] -translate-x-px">
							<LoadingSpinner />
						</div>
					</span>
				{:else if open}
					<button
						class="absolute inset-y-0 right-0 flex items-center pr-1.5"
						on:click|stopPropagation={() => (value = '')}
					>
						<icon
							class="h-4 w-4 text-base-10"
							data-icon="icon/x-mark"
							transition:fade={{ duration: 100 }}
						/>
					</button>
				{:else}
					<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-1.5">
						<icon
							class="h-4 w-4 text-base-10"
							data-icon="icon/magnifying-glass"
							transition:fade={{ duration: 100 }}
						/>
					</span>
				{/if}
			</div>

			{#if open && options.length > 0}
				<div class="absolute z-10 mt-1 w-full shadow-lg rounded-md bg-base-1">
					<ul
						class="overflow-auto max-h-36 rounded-md py-1 ring-1 ring-base-8 ring-opacity-5 focus:outline-none text-sm"
						role="listbox"
						tabindex="-1"
						aria-activedescendant="{ID}_{value}"
						transition:fade|local={{ duration: 75 }}
					>
						{#each options as name}
							{@const isSelected = value == name}
							{@const isHighlighted = highlighted == name}

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
								on:mouseenter={() => (highlighted = name)}
							>
								<button class="w-full text-left py-2 pl-3 pr-9" on:click={() => select(name)}>
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

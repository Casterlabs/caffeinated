<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import FocusListener from '$lib/interaction/FocusListener.svelte';

	import { createEventDispatcher, onMount } from 'svelte';
	import { fade } from 'svelte/transition';
	import { t } from '$lib/app.mjs';
	import { ALL_STREAMING_SERVICES } from '$lib/caffeinatedAuth.mjs';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let allowMultiple = false;
	export let requiredFeatures = [];
	export let value = ALL_STREAMING_SERVICES;

	let options = [];

	let open = false;
	let highlighted = null;

	$: open, open && (highlighted = value[0]);

	function select(id) {
		if (!allowMultiple) {
			return select(id);
		}

		if (value.indexOf(id) != -1 && allowMultiple) {
			// We actually want to REMOVE.
			value.splice(value.indexOf(id), 1);
			value = value;
			if (!allowMultiple) open = false;
			dispatch('value', value);
			return;
		}

		value.push(id);
		value = value;
		if (!allowMultiple) open = false;
		dispatch('value', value);
	}

	onMount(() => {
		if (typeof value == 'string') {
			value = [value];
		}

		Caffeinated.koi.features.then((featuresMap) => {
			for (const [platform, features] of Object.entries(featuresMap)) {
				if (!requiredFeatures || requiredFeatures.length == 0) {
					options.push(platform);
				} else {
					let meetsRequirements = true;
					for (const requiredFeature of requiredFeatures) {
						if (!features.includes(requiredFeature)) {
							meetsRequirements = false;
							break;
						}

						if (meetsRequirements) {
							options.push(platform);
						}
					}
				}
			}
			options = options;
		});
	});
</script>

<!-- svelte-ignore missing-declaration -->
<!-- svelte-ignore a11y-role-supports-aria-props -->
<!-- svelte-ignore a11y-no-static-element-interactions -->
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
		<button
			type="button"
			role="listbox"
			class="relative w-full h-[2.375rem] cursor-pointer rounded-md border border-base-7 bg-base-1 py-2 pl-3 pr-10 text-left shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
			aria-haspopup="listbox"
			aria-expanded={open}
			aria-labelledby={ID}
			on:click={() => (open = !open)}
		>
			<span class="block truncate text-base-12">
				{#await Promise.all(value.map(async (p) => await t(`co.casterlabs.caffeinated.app.platform.${p}`))) then mapped}
					{mapped.join(', ')}
				{/await}
				&nbsp;
			</span>
			<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-1">
				<icon class="h-5 w-5 text-base-10" data-icon="icon/chevron-up-down" />
			</span>
		</button>

		{#if open}
			<div class="absolute z-10 mt-1 max-h-60 w-full shadow-lg rounded-md bg-base-1">
				<ul
					class="overflow-auto h-full max-h-36 rounded-md py-1 ring-1 ring-base-8 ring-opacity-5 focus:outline-none text-sm"
					role="listbox"
					tabindex="-1"
					aria-activedescendant="{ID}_{value}"
					transition:fade|local={{ duration: 75 }}
				>
					{#each Array.from(new Set([...options, ...value])) as platform}
						{@const isSelected = value.includes(platform)}
						{@const isHighlighted = highlighted == platform}

						<li
							id="{ID}_{platform}"
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
							on:mouseenter={() => (highlighted = platform)}
						>
							<button class="w-full text-left py-2 pl-3 pr-9" on:click={() => select(platform)}>
								<LocalizedProperty
									key="co.casterlabs.caffeinated.app.platform.{platform}"
									property="title"
								/>
								<span class="block truncate" class:font-semibold={isSelected}>
									<LocalizedText key="co.casterlabs.caffeinated.app.platform.{platform}" />
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

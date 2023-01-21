<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Button from '$lib/ui/Button.svelte';
	import SlimSearchMenu from '$lib/ui/SlimSearchMenu.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';

	import { onMount } from 'svelte';

	let streamStateFeatures = {};
	let streamStates = {};
	let languages = {};

	let values = {};

	let currentTab = null;

	async function categorySearch(platform, query) {
		const response = await //
		(
			await fetch(
				`https://api.casterlabs.co/v2/koi/stream/${platform}/categories/search?q=${encodeURIComponent(
					query
				)}`
			)
		) //
			.json();

		const result = [];
		if (response.data) {
			for (const item of Object.values(response.data.result)) {
				result.push(item);
			}
		}
		return result;
	}

	onMount(() => {
		Caffeinated.auth.getLanguages().then((v) => (languages = v));

		Caffeinated.koi.svelte('streamStates').subscribe((v) => (streamStates = v));

		Caffeinated.koi.svelte('features').subscribe((features) => {
			// Look for loggged-out platforms.
			for (const [platform, state] of Object.entries(streamStates)) {
				if (features[platform]) {
					if (!values[platform]) {
						values[platform] = state; // Double check that we have data for that platform.
						state.language = languages[state.language]; // Patch for the language search.
					}

					continue; // Still here.
				}

				delete streamStateFeatures[platform];
				delete streamStates[platform];
				delete values[platform];

				if (currentTab == platform) {
					currentTab = null; // Unselect this platform. Will get set in the next loop.
				}
			}

			// Look for newly logged-in platforms.
			for (const [platform, featureList] of Object.entries(features)) {
				if (!featureList.includes('UPDATE_STREAM_INFO')) continue; // Platform does not support channel info updating.

				if (!currentTab) {
					currentTab = platform; // Select the first tab.
				}

				if (streamStateFeatures[platform]) continue; // We already have a list of features.

				// Ask Koi what the platform supports.
				fetch(`https://api.casterlabs.co/v2/koi/stream/${platform}/features`)
					.then((response) => response.json())
					.then((json) => {
						streamStateFeatures[platform] = json.data.features;
						streamStateFeatures = streamStateFeatures; // Re-render.
					});
			}
		});
	});
</script>

<div class="flex flex-col h-full">
	<ul
		class="flex-0 flex justify-center space-x-2 py-2 bg-base-2 border-b border-base-5"
		role="navigation"
	>
		{#each Object.keys(streamStateFeatures) as platform}
			{@const isSelected = currentTab == platform}
			<li class="inline-block">
				<button
					type="button"
					class="relative w-fit h-[2.375rem] cursor-pointer rounded-md py-1.5 px-2 transition-[background-color] bg-base-2 border border-base-6 hover:bg-base-4 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
					class:bg-primary-10={isSelected}
					class:hover:bg-primary-11={isSelected}
					class:text-primary-1={isSelected}
					on:click={() => (currentTab = platform)}
				>
					<span class="sr-only">{platform.toLowerCase()}</span>
					<icon data-icon="service/{platform.toLowerCase()}" />
				</button>
			</li>
		{/each}
	</ul>

	<div class="flex-1 px-4 pt-4 w-full max-w-xs mx-auto space-y-4">
		{#if streamStateFeatures[currentTab] && values[currentTab]}
			{@const platform = currentTab}
			{@const features = streamStateFeatures[platform]}

			{#if features.includes('TITLE')}
				<div>
					<p class="text-sm font-medium text-base-12 mb-0.5">
						<LocalizedText key="channel_info.title" />
					</p>
					<TextArea
						rows="1"
						resize={false}
						placeholder="Live on {platform}!"
						bind:value={values[platform].title}
					/>
				</div>
			{/if}

			{#if features.includes('CATEGORY')}
				<div>
					<p class="text-sm font-medium text-base-12 mb-0.5">
						<LocalizedText key="channel_info.category" />
					</p>
					<SlimSearchMenu
						search={(query) => categorySearch(platform, query)}
						width="full"
						localize={false}
						bind:value={values[platform].category}
					/>
				</div>
			{/if}

			{#if features.includes('LANGUAGE')}
				<div>
					<p class="text-sm font-medium text-base-12 mb-0.5">
						<LocalizedText key="channel_info.language" />
					</p>
					<SlimSearchMenu
						search={(query) =>
							Object.values(languages).filter((v) => v.toLowerCase().includes(query.toLowerCase()))}
						width="full"
						localize={false}
						bind:value={values[platform].language}
					/>
				</div>
			{/if}

			{#if features.includes('TAGS')}
				<div>
					<p class="text-sm font-medium text-base-12 mb-0.5">
						<LocalizedText key="channel_info.tags" />
					</p>
					// TODO TAGS
				</div>
			{/if}

			{#if features.includes('CONTENT_RATING')}
				<div>
					<p class="text-sm font-medium text-base-12 -mb-2">
						<LocalizedText key="channel_info.content_rating" />
					</p>
					<SlimSelectMenu
						options={{
							FAMILY_FRIENDLY: 'Family Friendly',
							PG: 'PG',
							MATURE: 'Mature'
						}}
						width="full"
						localize={false}
						bind:value={values[platform].content_rating}
					/>
				</div>
			{/if}

			{#if features.includes('THUMBNAIL')}
				<div>
					<p class="text-sm font-medium text-base-12 mb-0.5">
						<LocalizedText key="channel_info.thumbnail" />
					</p>
					// TODO THUMBNAIL
				</div>
			{/if}

			<button
				type="button"
				class="relative w-full h-[2.375rem] cursor-pointer rounded-md py-1.5 px-2 shadow-sm transition-[background-color] bg-base-1 border border-base-6 hover:bg-base-3 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
				on:click={() => {}}
			>
				<LocalizedText key="channel_info.update" />
			</button>
		{/if}
	</div>
</div>

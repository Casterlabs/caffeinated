<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import SlimSearchMenu from '$lib/ui/SlimSearchMenu.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';
	import ThumbnailPicker from './ThumbnailPicker.svelte';

	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('ChannelInfo');

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

	async function update() {
		let languageEnum = null;
		for (const [language, lang] of Object.entries(languages)) {
			if (lang == values[currentTab].language) {
				languageEnum = language;
				break;
			}
		}

		let categoryId = null;
		{
			let query = values[currentTab].category;

			if (query == 'Entertainment' && currentTab == 'CAFFEINE') {
				categoryId = '79';
			} else {
				// Lookup category id from name.
				// TODO Make this more error resistant.
				const response = await //
				(
					await fetch(
						`https://api.casterlabs.co/v2/koi/stream/${currentTab}/categories/search?q=${encodeURIComponent(
							query
						)}`
					)
				) //
					.json();
				const result = Object.entries(response.data.result);
				console.log(result);
				categoryId = result[0][0];
			}
		}

		const streamUpdatePayload = {
			title: values[currentTab].title,
			category: categoryId,
			language: languageEnum,
			tags: values[currentTab].tags,
			content_rating: values[currentTab].content_rating
		};

		let token;
		{
			const authInstances = Object.values(await Caffeinated.auth.authInstances);
			for (const authInstance of authInstances) {
				if (authInstance.streamData.streamer.UPID == streamStates[currentTab].streamer.UPID) {
					token = authInstance.token;
					break;
				}
			}
		}

		if (!token) {
			console.error("Couldn't find token, aborting.");
			return;
		}

		console.debug('Update:', streamUpdatePayload);

		fetch('https://api.casterlabs.co/v2/koi/stream/update', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'Client-ID': 'LmHG2ux992BxqQ7w9RJrfhkW',
				Authorization: 'Koi ' + token
			},
			body: JSON.stringify(streamUpdatePayload)
		});

		if (values[currentTab].thumbnail) {
			console.debug('Update:', values[currentTab].thumbnail);
			fetch('https://api.casterlabs.co/v2/koi/stream/thumbnail/update', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/octet-stream',
					'Client-ID': 'LmHG2ux992BxqQ7w9RJrfhkW',
					Authorization: 'Koi ' + token
				},
				body: values[currentTab].thumbnail
			});
		}
	}

	onMount(() => {
		Caffeinated.auth.getLanguages().then((v) => (languages = v));

		window.svelte('Caffeinated.koi', 'streamStates').subscribe((v) => (streamStates = v));

		window.svelte('Caffeinated.koi', 'features').subscribe((features) => {
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

	<div class="flex-1 w-full overflow-y-auto">
		<div class="w-full max-w-xs mx-auto px-4 py-4">
			{#if streamStateFeatures[currentTab] && values[currentTab]}
				{@const platform = currentTab}
				{@const features = streamStateFeatures[platform]}

				<div class="space-y-4">
					{#if features.includes('TITLE')}
						<div>
							<p class="text-sm font-medium text-base-12 mb-0.5">
								<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.title" />
							</p>
							<TextArea
								rows="1"
								resize={true}
								placeholder="Live on {platform}!"
								bind:value={values[platform].title}
							/>
						</div>
					{/if}

					{#if features.includes('CATEGORY')}
						<div>
							<p class="text-sm font-medium text-base-12 mb-0.5">
								<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.category" />
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
								<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.language" />
							</p>
							<SlimSearchMenu
								search={(query) =>
									Object.values(languages).filter((v) =>
										v.toLowerCase().includes(query.toLowerCase())
									)}
								width="full"
								localize={false}
								bind:value={values[platform].language}
							/>
						</div>
					{/if}

					{#if features.includes('TAGS')}
						<div>
							<p class="text-sm font-medium text-base-12 mb-0.5">
								<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.tags" />
							</p>

							<div class="-mx-0.5">
								{#each values[platform].tags as tag, idx}
									<span
										class="mx-0.5 inline-flex items-center rounded-full bg-base-5 px-2.5 py-0.5 text-xs font-medium text-base-12"
									>
										{tag}

										<button
											class="translate-y-px translate-x-1"
											on:click={() => {
												values[platform].tags.splice(idx, 1);
												values = values; // Rerender
											}}
										>
											<span class="sr-only">
												<LocalizedText
													key="co.casterlabs.caffeinated.app.docks.channel_info.tags.remove"
												/>
											</span>
											<icon class="w-3 h-3" data-icon="icon/x-mark" />
										</button>
									</span>
								{/each}
							</div>
							<!-- svelte-ignore a11y-no-static-element-interactions -->
							<div
								class="mt-1.5 relative"
								on:keydown={(e) => {
									if (e.key == ' ') {
										e.preventDefault();
										return;
									}

									if (e.key != 'Enter') return;
									e.preventDefault();
									e.target.parentElement.querySelector('button').click();
								}}
							>
								<TextArea rows="1" resize={false} placeholder="channel_info.tags.add.description" />

								<button
									class="absolute inset-y-0 right-2"
									on:click={(e) => {
										if (values[platform].tags.length >= 10) {
											return; // Ignore.
										}

										const textarea = e.target.parentElement.querySelector('textarea');

										if (values[platform].tags.includes(textarea.value)) {
											return; // Duplicate.
										}

										values[platform].tags.push(textarea.value);
										textarea.value = '';
										values = values; // Rerender
									}}
								>
									<LocalizedProperty
										key="co.casterlabs.caffeinated.app.docks.channel_info.tags.add"
										property="title"
									/>
									<span class="sr-only">
										<LocalizedText
											key="co.casterlabs.caffeinated.app.docks.channel_info.tags.add"
										/>
									</span>
									<icon class="w-4 h-4" data-icon="icon/plus" />
								</button>
							</div>
						</div>
					{/if}

					{#if features.includes('CONTENT_RATING')}
						<div>
							<p class="text-sm font-medium text-base-12 -mb-2">
								<LocalizedText
									key="co.casterlabs.caffeinated.app.docks.channel_info.content_rating"
								/>
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
								<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.thumbnail" />
							</p>

							<ThumbnailPicker values={values[platform]} />
						</div>
					{/if}
				</div>

				<button
					type="button"
					class="mt-6 relative w-full h-[2.375rem] cursor-pointer rounded-md py-1.5 px-2 shadow-sm transition-[background-color] bg-base-1 border border-base-6 hover:bg-base-3 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
					on:click={update}
				>
					<LocalizedText key="co.casterlabs.caffeinated.app.docks.channel_info.update" />
				</button>
			{/if}
		</div>
	</div>
</div>

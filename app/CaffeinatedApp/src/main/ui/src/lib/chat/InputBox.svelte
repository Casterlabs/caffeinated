<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import FocusListener from '$lib/interaction/FocusListener.svelte';

	import { createEventDispatcher, onMount } from 'svelte';
	import { fade } from 'svelte/transition';
	import { writable } from 'svelte/store';
	import { t } from '$lib/app.mjs';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let userStates = {};
	export let supportedFeatures = {};
	export let replyTarget = null;
	export let preferences = {};

	let messageHistory = [];
	let historyIndex = -1;
	let messageEdited = false;
	let platform = 'TWITCH';
	let message = '';

	$: preferences,
		(() => {
			if (preferences?.platform) {
				platform = preferences.platform;
			}
		})();

	$: isSupportedByPlatform = supportedFeatures[platform]?.includes('CHAT_SEND_MESSAGE');
	$: isMultiAccountMode = Object.keys(userStates || {}).length > 1;

	function send() {
		if (!isMultiAccountMode) {
			// Always default back to the first in the list.
			platform = Object.keys(userStates)[0];
		}

		const data = { message, platform, replyTarget };
		dispatch('send', data);
		messageHistory.push(data);
		message = '';
		historyIndex = -1;
		messageEdited = false;
	}

	let selectorOpen = false;
	let selectorHighlighted = null;

	$: selectorOpen, selectorOpen && (selectorHighlighted = platform);

	function select(id) {
		platform = id;
		selectorOpen = false;
		preferences.platform = id;
		dispatch('save', preferences);
	}

	let placeholderT = writable('');
	onMount(() =>
		t('co.casterlabs.caffeinated.app.docks.chat.viewer.send_message.placeholder').then(
			placeholderT.set
		)
	);
</script>

<div class="flex flex-row h-fit">
	{#if isMultiAccountMode}
		<div class="flex-0 -mr-px">
			<FocusListener class="relative" on:lostfocus={() => (selectorOpen = false)}>
				<!-- svelte-ignore a11y-no-static-element-interactions -->
				<div
					on:keyup={(e) => {
						if (e.code == 'Enter') {
							// Handle the enter key if the user has a highlighted option.
							if (selectorHighlighted) {
								select(selectorHighlighted);
							}
						} else if (e.code == 'ArrowDown' || e.code == 'ArrowUp') {
							// ArrowDown: 1, ArrowUp: -1.
							// 1: Descending, -1: Ascending.
							const direction = e.code == 'ArrowUp' ? -1 : 1;
							const keys = Object.keys(userStates);

							// If we have a highlighted option, then we want to move based on that.
							if (selectorHighlighted) {
								let curr = keys.indexOf(selectorHighlighted);
								curr += direction;

								// Wrap around.
								if (curr < 0) {
									selectorHighlighted = keys[keys.length - 1];
								} else if (curr >= keys.length) {
									selectorHighlighted = keys[0];
								}
								// Navigate to the next item.
								else {
									selectorHighlighted = keys[curr];
								}
							} else {
								// Otherwise, snap to the top or bottom of the list.
								if (direction == 1) {
									selectorHighlighted = keys[0];
								} else {
									selectorHighlighted = keys[keys.length - 1];
								}
							}
						}
					}}
				>
					<!-- svelte-ignore a11y-role-supports-aria-props -->
					<button
						type="button"
						role="listbox"
						class="relative w-full h-[2.375rem] cursor-pointer rounded-l-md border border-base-7 bg-base-1 py-2 pl-3 pr-7 text-left shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
						aria-haspopup="listbox"
						aria-expanded={selectorOpen}
						on:click={() => (selectorOpen = !selectorOpen)}
					>
						{#key platform}
							{#if platform == null}
								<LocalizedProperty
									key="co.casterlabs.caffeinated.app.docks.chat.viewer.all_platforms"
									property="title"
								/>
								<LocalizedText
									key="co.casterlabs.caffeinated.app.docks.chat.viewer.all_platforms"
								/>
							{:else}
								<LocalizedProperty
									key={isSupportedByPlatform
										? `${platform} (${userStates[platform]?.streamer?.displayname})`
										: 'co.casterlabs.caffeinated.app.unsupported_feature.item'}
									opts={{ item: platform }}
									property="title"
								/>
								<span class="block truncate text-base-12">
									<icon
										class="w-4 h-5"
										class:opacity-60={!isSupportedByPlatform}
										data-icon="service/{platform.toLowerCase()}"
									/>
								</span>
							{/if}
						{/key}
						<span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-1">
							<icon class="h-5 w-5 text-base-10" data-icon="icon/chevron-up-down" />
						</span>
					</button>

					{#if selectorOpen}
						<div
							class="absolute bottom-full mb-1 z-10 max-h-60 w-full shadow-lg rounded-md bg-base-1"
						>
							<ul
								class="overflow-auto max-h-36 rounded-md py-1 ring-1 ring-base-8 ring-opacity-5 focus:outline-none text-sm"
								role="listbox"
								tabindex="-1"
								aria-activedescendant="{ID}_{platform}"
								transition:fade|local={{ duration: 75 }}
							>
								{#each [null, ...Object.keys(userStates)] as name}
									{@const isSelected = platform == name}
									{@const isHighlighted = selectorHighlighted == name}
									{@const isSupported =
										!name || supportedFeatures[name]?.includes('CHAT_SEND_MESSAGE')}

									<li
										id="{ID}_{name}"
										class="font-normal relative cursor-default select-none"
										class:text-base-12={!isHighlighted}
										class:bg-primary-9={isHighlighted}
										class:text-white={isHighlighted}
										role="option"
										aria-selected={isSelected}
										on:mouseleave={() => {
											if (isHighlighted) {
												selectorHighlighted = null;
											}
										}}
										on:mouseenter={() => (selectorHighlighted = name)}
									>
										<button
											class="w-full py-2 pl-3 pr-9"
											class:opacity-60={!isSupported}
											on:click={() => {
												if (isSupported) {
													select(name);
												}
											}}
										>
											{#if name == null}
												<LocalizedProperty
													key="co.casterlabs.caffeinated.app.docks.chat.viewer.all_platforms"
													property="title"
												/>
												<LocalizedText
													key="co.casterlabs.caffeinated.app.docks.chat.viewer.all_platforms"
												/>
											{:else}
												<LocalizedProperty
													key={isSupported
														? `${name} (${userStates[name]?.streamer?.displayname})`
														: 'co.casterlabs.caffeinated.app.unsupported_feature.item'}
													opts={{ item: name }}
													property="title"
												/>
												<icon
													class="translate-y-px w-4 h-4"
													data-icon="service/{name.toLowerCase()}"
												/>
											{/if}

											{#if isSelected}
												<span
													class="absolute inset-y-0 right-0 flex items-center pr-1"
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
	{/if}

	<div class="flex-1 relative">
		<textarea
			class="px-2.5 py-2 resize-none block w-full text-base-12 border transition hover:border-base-8 border-base-7 bg-base-1 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
			class:rounded-l-md={!isMultiAccountMode}
			class:opacity-60={!isSupportedByPlatform && platform}
			placeholder={$placeholderT}
			rows="1"
			resize={false}
			bind:value={message}
			on:keyup={(e) => {
				if (message.length == 0) {
					messageEdited = false; // Clear the state when the box gets cleared.
				}
			}}
			on:keydown={(e) => {
				switch (e.key) {
					case 'Enter': {
						if (e.shiftKey) return;
						e.preventDefault();
						send(); // Trigger a send on Enter, but just a regular newline on Shift+Enter.
						return;
					}

					case 'ArrowUp': {
						if (messageEdited) return; // Do not replace.
						if (messageHistory.length == 0) return; // No messages to pull.

						if (historyIndex == -1) {
							historyIndex = messageHistory.length - 1;
						} else {
							historyIndex--;
							if (historyIndex < 0) {
								historyIndex = 0; // Lower bounds.
							}
						}

						message = messageHistory[historyIndex].message;
						platform = messageHistory[historyIndex].platform;
						replyTarget = messageHistory[historyIndex].replyTarget;
						return; // Do not mark as edited.
					}

					case 'ArrowDown': {
						if (messageEdited) return; // Do not replace.
						if (messageHistory.length == 0) return; // No messages to pull.

						if (historyIndex == -1) {
							historyIndex = messageHistory.length - 1;
						} else {
							historyIndex++;
							if (historyIndex == messageHistory.length) {
								historyIndex = messageHistory.length - 1; // Upper bounds.
							}
						}

						message = messageHistory[historyIndex].message;
						platform = messageHistory[historyIndex].platform;
						replyTarget = messageHistory[historyIndex].replyTarget;
						return; // Do not mark as edited.
					}
				}

				messageEdited = true;
			}}
		/>
		<slot />
	</div>

	<div class="flex-0" class:opacity-60={!isSupportedByPlatform}>
		<button
			type="button"
			class="relative w-fit h-[2.375rem] cursor-pointer -ml-px rounded-r-md py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
			on:click={send}
		>
			<LocalizedProperty
				key={isSupportedByPlatform
					? undefined
					: 'co.casterlabs.caffeinated.app.unsupported_feature.item'}
				property="title"
				opts={{ item: platform }}
			/>
			<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.send_message" />
		</button>
	</div>
</div>

<style>
	textarea::placeholder {
		color: var(--base8);
	}
</style>

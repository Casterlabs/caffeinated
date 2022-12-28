<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import FocusListener from '$lib/interaction/FocusListener.svelte';

	import { createEventDispatcher } from 'svelte';
	import { fade } from 'svelte/transition';
	import { t } from '$lib/translate.mjs';

	const ID = Math.random().toString(36);
	const dispatch = createEventDispatcher();

	export let userStates = {};
	export let replyTarget = null;

	let platform = 'TWITCH';
	let message = '';

	$: isMultiAccountMode = Object.keys(userStates || {}).length > 1;

	function send() {
		if (!isMultiAccountMode) {
			// Always default back to the first in the list.
			platform = Object.keys(userStates)[0];
		}

		dispatch('send', { message, platform, replyTarget });
	}

	let selectorOpen = false;
	let selectorHighlighted = null;

	$: selectorOpen, selectorOpen && (selectorHighlighted = platform);

	function select(id) {
		platform = id;
		selectorOpen = false;
	}
</script>

<div class="flex flex-row h-fit">
	{#if isMultiAccountMode}
		<div class="flex-0 -mr-px">
			<FocusListener class="relative" on:lostfocus={() => (selectorOpen = false)}>
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
					<button
						type="button"
						role="listbox"
						class="relative w-full h-[2.375rem] cursor-pointer rounded-l-md border border-base-7 bg-base-1 py-2 pl-3 pr-7 text-left shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
						title="{platform} ({userStates[platform]?.streamer?.displayname})"
						aria-haspopup="listbox"
						aria-expanded={selectorOpen}
						on:click={() => (selectorOpen = !selectorOpen)}
					>
						<span class="block truncate text-base-12">
							{#key platform}
								<icon class="w-4 h-5" data-icon="service/{platform.toLowerCase()}" />
							{/key}
						</span>
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
								{#each Object.keys(userStates) as name}
									{@const isSelected = platform == name}
									{@const isHighlighted = selectorHighlighted == name}

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
											title="{name} ({userStates[name]?.streamer?.displayname})"
											on:click={() => select(name)}
										>
											<icon
												class="translate-y-px w-4 h-4"
												data-icon="service/{name.toLowerCase()}"
											/>

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

	<div class="flex-1">
		<textarea
			class="px-2.5 py-2 resize-none block w-full text-base-12 border transition hover:border-base-8 border-base-7 bg-base-1 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-sm"
			class:rounded-l-md={!isMultiAccountMode}
			placeholder={t('chat.viewer.send_message.placeholder')}
			rows="1"
			resize={false}
			bind:value={message}
			on:keyup={(e) => {
				if (e.key == 'Enter' && !e.shiftKey) {
					e.preventDefault();
					send(); // Trigger a send on Enter, but just a regular newline on Shift+Enter.
					message = '';
				}
			}}
		/>
	</div>

	<div class="flex-0">
		<button
			type="button"
			class="relative w-fit h-[2.375rem] cursor-pointer -ml-px rounded-r-md py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-sm"
			on:click={send}
		>
			<LocalizedText key="chat.viewer.send_message" />
		</button>
	</div>
</div>

<style>
	textarea::placeholder {
		color: var(--mauve8);
	}
</style>

<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';
	import Container from '$lib/ui/Container.svelte';
	import CodeInput from '$lib/ui/CodeInput.svelte';

	import { t } from '$lib/app.mjs';
	import { STREAMING_SERVICE_NAMES } from '$lib/caffeinatedAuth.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import Debouncer from '$lib/debouncer.mjs';

	import EDITOR_TYPES from '$lib/chatBotTypes.d.ts?raw';

	const debouncer = new Debouncer();

	const PLATFORMS = {
		[null]: 'co.casterlabs.caffeinated.app.page.chat_bot.platform.ANY',
		...STREAMING_SERVICE_NAMES
	};

	const EVENT_TYPES = {
		DONATION: 'co.casterlabs.caffeinated.app.page.chat_bot.shouts.DONATION',
		FOLLOW: 'co.casterlabs.caffeinated.app.page.chat_bot.shouts.FOLLOW',
		RAID: 'co.casterlabs.caffeinated.app.page.chat_bot.shouts.RAID',
		SUBSCRIPTION: 'co.casterlabs.caffeinated.app.page.chat_bot.shouts.SUBSCRIPTION'
	};

	const RESPONSE_ACTIONS = {
		REPLY_WITH: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.response_action.REPLY_WITH',
		EXECUTE: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.response_action.EXECUTE'
	};

	const console = createConsole('Chat Bot/Shouts');
	const preferences = st || window.svelte('Caffeinated.chatbot', 'preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);
	$: shouts = $preferences?.shouts || [];

	let codeExpandedOn = null;

	function saveDB() {
		debouncer.debounce(save);
	}

	async function save() {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			shouts: shouts
		};
	}
</script>

<ul class="space-y-4">
	{#each shouts as shout, idx}
		<li class="relative">
			<Container>
				<LocalizedText
					key="co.casterlabs.caffeinated.app.page.chat_bot.shouts.format"
					slotMapping={['platform', 'action', 'response_action', 'response']}
				>
					<span class="inline-block h-fit" slot="0">
						<SlimSelectMenu bind:value={shout.platform} options={PLATFORMS} on:value={saveDB} />
					</span>

					<span class="inline-block h-fit" slot="1">
						<SlimSelectMenu bind:value={shout.eventType} options={EVENT_TYPES} on:value={saveDB} />
					</span>

					<span class="inline-block h-fit" slot="2">
						<SlimSelectMenu
							bind:value={shout.responseAction}
							options={RESPONSE_ACTIONS}
							on:value={async ({ detail: value }) => {
								// Swap the examples to make sense.
								switch (value) {
									case 'REPLY_WITH': {
										if (
											shout.response ==
											t('co.casterlabs.caffeinated.app.page.chat_bot.shouts.example.SCRIPT')
										) {
											shout.trigger = 'casterlabs';
											shout.response = await t(
												'co.casterlabs.caffeinated.app.page.chat_bot.shouts.example'
											);
										}
										break;
									}
									case 'EXECUTE': {
										if (
											shout.response ==
											t('co.casterlabs.caffeinated.app.page.chat_bot.shouts.example')
										) {
											shout.trigger = 'test';
											shout.response = await t(
												'co.casterlabs.caffeinated.app.page.chat_bot.shouts.example.SCRIPT'
											);
										}
										break;
									}
								}
								saveDB();
							}}
						/>
					</span>

					<span slot="3" class="block mt-1.5">
						{#if shout.responseAction == 'EXECUTE'}
							<div class="relative h-20" class:h-[70vh]={codeExpandedOn == shout}>
								<CodeInput
									bind:value={shout.response}
									on:value={saveDB}
									language="javascript"
									typescriptTypings={EDITOR_TYPES}
								/>

								<button
									class="absolute right-0 bottom-0 p-0.5 rounded-tl bg-base-2 hover:bg-base-7 border-t border-l border-base-8 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-base-12"
									on:click={() => (codeExpandedOn = codeExpandedOn == shout ? null : shout)}
								>
									{#if codeExpandedOn == shout}
										<icon class="h-5 w-5" data-icon="icon/arrows-pointing-in" />
									{:else}
										<icon class="h-5 w-5" data-icon="icon/arrows-pointing-out" />
									{/if}
								</button>
							</div>
						{:else}
							<TextArea
								placeholder="co.casterlabs.caffeinated.app.page.chat_bot.shouts.example"
								rows={2}
								bind:value={shout.response}
								on:value={saveDB}
							/>
						{/if}
					</span>
				</LocalizedText>
			</Container>

			<button
				class="absolute top-2 right-1 text-error hover:opacity-80"
				on:click={() => {
					shouts.splice(idx, 1);
					save();
					shouts = shouts;
				}}
			>
				<LocalizedProperty
					key="co.casterlabs.caffeinated.app.page.chat_bot.remove"
					property="title"
				/>
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.remove" />
				</span>
				<icon class="h-5" data-icon="icon/trash" />
			</button>
		</li>
	{/each}

	<li>
		<button
			class="mt-2 w-full relative flex items-center justify-center rounded-lg transition hover:text-base-12 text-base-11 border hover:border-base-8 border-base-7 hover:bg-base-3 bg-base-2 py-1 px-2 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			on:click={async () => {
				shouts.push({
					platform: null,
					eventType: 'FOLLOW',
					text: await t('co.casterlabs.caffeinated.app.page.chat_bot.shouts.example')
				});
				save();
				shouts = shouts;
			}}
		>
			<LocalizedProperty
				key="co.casterlabs.caffeinated.app.page.chat_bot.commands.add"
				property="title"
			/>
			<div>
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.commands.add" />
				</span>
				<icon data-icon="icon/plus" />
			</div>
		</button>
	</li>
</ul>

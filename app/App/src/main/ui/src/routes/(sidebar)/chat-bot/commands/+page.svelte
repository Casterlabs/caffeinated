<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';
	import Container from '$lib/ui/Container.svelte';
	import SlimButton from '$lib/ui/SlimButton.svelte';
	import CodeInput from '$lib/ui/CodeInput.svelte';

	import { t } from '$lib/app.mjs';
	import { STREAMING_SERVICE_NAMES } from '$lib/caffeinatedAuth.mjs';
	import { onMount, tick } from 'svelte';
	import Debouncer from '$lib/debouncer.mjs';
	import createConsole from '$lib/console-helper.mjs';

	import EDITOR_TYPES from '$lib/chatBotTypes.d.ts?raw';

	const debouncer = new Debouncer();

	const PLATFORMS = {
		[null]: 'co.casterlabs.caffeinated.app.page.chat_bot.platform.ANY',
		...STREAMING_SERVICE_NAMES
	};

	const TRIGGER_TYPES = {
		COMMAND: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.trigger_type.COMMAND',
		CONTAINS: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.trigger_type.CONTAINS',
		ALWAYS: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.trigger_type.ALWAYS'
	};

	const RESPONSE_ACTIONS = {
		REPLY_WITH: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.response_action.REPLY_WITH',
		EXECUTE: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.response_action.EXECUTE'
	};

	const console = createConsole('Chat Bot/Commands');
	let commands = [];

	let codeExpandedOn = null;

	onMount(async () => {
		Caffeinated.chatbot.preferences.then((preferences) => {
			commands = preferences.commands;
		});
	});

	function saveDB() {
		debouncer.debounce(save);
	}

	async function save() {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			commands: commands
		};
	}
</script>

<ul class="space-y-4">
	{#each commands as command, idx}
		<li class="relative">
			<Container>
				<LocalizedText
					key="co.casterlabs.caffeinated.app.page.chat_bot.commands.format.{command.triggerType}"
					slotMapping={['platform', 'trigger_type', 'trigger', 'response_action', 'response']}
				>
					<span class="inline-block h-fit" slot="0">
						<SlimSelectMenu bind:value={command.platform} options={PLATFORMS} on:value={saveDB} />
					</span>

					<span class="inline-block h-fit" slot="1">
						<SlimSelectMenu
							bind:value={command.triggerType}
							options={TRIGGER_TYPES}
							on:value={saveDB}
						/>
					</span>

					<span
						class="inline-block h-7 w-32 align-top"
						slot="2"
						class:hidden={command.triggerType == 'ALWAYS'}
					>
						<SlimTextArea
							placeholder=""
							rows="1"
							resize={false}
							bind:value={command.trigger}
							on:value={saveDB}
						/>
					</span>

					<span class="inline-block h-fit" slot="3">
						<SlimSelectMenu
							bind:value={command.responseAction}
							options={RESPONSE_ACTIONS}
							on:value={async ({ detail: value }) => {
								// Swap the examples to make sense.
								switch (value) {
									case 'REPLY_WITH': {
										if (
											command.response ==
											(await t(
												'co.casterlabs.caffeinated.app.page.chat_bot.commands.example.SCRIPT'
											))
										) {
											command.trigger = 'casterlabs';
											command.response = await t(
												'co.casterlabs.caffeinated.app.page.chat_bot.commands.example'
											);
										}
										break;
									}
									case 'EXECUTE': {
										if (
											command.response ==
											(await t('co.casterlabs.caffeinated.app.page.chat_bot.commands.example'))
										) {
											command.trigger = 'test';
											command.response = await t(
												'co.casterlabs.caffeinated.app.page.chat_bot.commands.example.SCRIPT'
											);
										}
										break;
									}
								}
								saveDB();
							}}
						/>
					</span>

					<span slot="4" class="block mt-1.5">
						{#if command.responseAction == 'EXECUTE' || command.triggerType == 'ALWAYS'}
							<div class="relative h-20" class:h-[70vh]={codeExpandedOn == command}>
								<CodeInput
									bind:value={command.response}
									on:value={saveDB}
									language="javascript"
									typescriptTypings={EDITOR_TYPES}
								/>

								<button
									class="absolute right-0 bottom-0 p-0.5 rounded-tl bg-base-2 hover:bg-base-7 border-t border-l border-base-8 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-base-12"
									on:click={() => (codeExpandedOn = codeExpandedOn == command ? null : command)}
								>
									{#if codeExpandedOn == command}
										<icon class="h-5 w-5" data-icon="icon/arrows-pointing-in" />
									{:else}
										<icon class="h-5 w-5" data-icon="icon/arrows-pointing-out" />
									{/if}
								</button>
							</div>
						{:else}
							<TextArea
								placeholder="co.casterlabs.caffeinated.app.page.chat_bot.commands.example"
								rows={2}
								bind:value={command.response}
								on:value={saveDB}
							/>
						{/if}
					</span>
				</LocalizedText>
			</Container>

			<button
				class="absolute top-2 right-1 text-error hover:opacity-80"
				on:click={() => {
					// Yucky code to avoid rerender bugs.
					let interim = commands;
					commands = [];

					interim.splice(idx, 1);
					tick().then(() => (commands = interim));
					save();
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
				commands.push({
					platform: null,
					triggerType: 'COMMAND',
					trigger: 'casterlabs',
					responseAction: 'REPLY_WITH',
					response: await t('co.casterlabs.caffeinated.app.page.chat_bot.commands.example')
				});
				commands = commands;
				save();
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

<div class="mt-4">
	<h1 class="font-bold text-lg">
		<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.commands.examples" />
	</h1>

	<ul class="space-y-4">
		<!-- Script Examples -->
		{#each ['song', 'shoutout', 'sound', 'tts'] as example}
			<li>
				<h2 class="font-semibold">
					!{example}

					<SlimButton
						on:click={async () => {
							const trigger = example;
							const code = await t(
								`co.casterlabs.caffeinated.app.page.chat_bot.commands.examples.${example}.code`
							);

							commands.push({
								platform: null,
								triggerType: 'COMMAND',
								trigger: trigger,
								responseAction: 'EXECUTE',
								response: code
							});
							commands = commands;
							save();
						}}
					>
						<LocalizedText
							key="co.casterlabs.caffeinated.app.page.chat_bot.commands.examples.add"
						/>
					</SlimButton>
				</h2>
				<p>
					<LocalizedText
						key="co.casterlabs.caffeinated.app.page.chat_bot.commands.examples.{example}.description"
					/>
				</p>
			</li>
		{/each}
	</ul>
</div>

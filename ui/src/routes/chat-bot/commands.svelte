<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import SlimSelectMenu from '$lib/ui/SlimSelectMenu.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';
	import Container from '$lib/ui/Container.svelte';

	import { t } from '$lib/translate.mjs';
	import { STREAMING_SERVICE_NAMES } from '../../components/caffeinatedAuth.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import Debouncer from '$lib/debouncer.mjs';
	import SlimButton from '$lib/ui/SlimButton.svelte';
	import CodeInput from '$lib/ui/CodeInput.svelte';

	const debouncer = new Debouncer();

	const PLATFORMS = {
		[null]: 'page.chat_bot.platform.ANY',
		...STREAMING_SERVICE_NAMES
	};

	const TYPES = {
		COMMAND: 'page.chat_bot.commands.type.COMMAND',
		CONTAINS: 'page.chat_bot.commands.type.CONTAINS',
		SCRIPT: 'page.chat_bot.commands.type.SCRIPT'
	};

	const console = createConsole('Chat Bot/Commands');
	const preferences = st || Caffeinated.chatbot.svelte('preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);

	$: commands = $preferences?.commands || [];

	let codeExpandedOn = null;

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
					key="page.chat_bot.commands.format.{command.type}"
					slotMapping={['platform', 'action', 'action_target', 'message']}
				>
					<span class="inline-block h-fit" slot="0">
						<SlimSelectMenu bind:value={command.platform} options={PLATFORMS} on:value={saveDB} />
					</span>

					<span class="inline-block h-fit" slot="1">
						<SlimSelectMenu
							bind:value={command.type}
							options={TYPES}
							on:value={({ detail: value }) => {
								if (value == 'SCRIPT' && command.response == t('page.chat_bot.commands.example')) {
									command.trigger = 'test';
									command.response = t('page.chat_bot.commands.example.SCRIPT');
								}
								saveDB();
							}}
						/>
					</span>

					<div class="w-28 inline-block translate-y-3" slot="2">
						<SlimTextArea
							placeholder=""
							rows="1"
							resize={false}
							bind:value={command.trigger}
							on:value={saveDB}
						/>
					</div>

					<span slot="3" class="block mt-1.5">
						{#if command.type == 'SCRIPT'}
							<div class="relative h-20" class:h-[70vh]={codeExpandedOn == command}>
								<CodeInput bind:value={command.response} on:value={saveDB} language="javascript" />

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

							<span class="mt-4">
								<LocalizedText key="sdk_documentation" />
							</span>
						{:else}
							<TextArea
								placeholder="page.chat_bot.commands.example"
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
				title={t('sr.page.chat_bot.remove')}
				on:click={() => {
					commands.splice(idx, 1);
					save();
					commands = commands;
				}}
			>
				<span class="sr-only">
					<LocalizedText key="sr.page.chat_bot.remove" />
				</span>
				<icon class="h-5" data-icon="icon/trash" />
			</button>
		</li>
	{/each}

	<li>
		<button
			class="mt-2 w-full relative flex items-center justify-center rounded-lg transition hover:text-base-12 text-base-11 border hover:border-base-8 border-base-7 hover:bg-base-3 bg-base-2 py-1 px-2 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			title={t('sr.page.chat_bot.commands.add')}
			on:click={() => {
				commands.push({
					platform: null,
					trigger: 'casterlabs',
					response: t('page.chat_bot.commands.example'),
					type: 'COMMAND'
				});
				commands = commands;
				save();
			}}
		>
			<div>
				<span class="sr-only">
					<LocalizedText key="sr.page.chat_bot.commands.add" />
				</span>
				<icon data-icon="icon/plus" />
			</div>
		</button>
	</li>
</ul>

<div class="mt-4">
	<h1 class="font-bold text-lg">
		<LocalizedText key="page.chat_bot.commands.examples" />
	</h1>

	<ul class="space-y-4">
		<!-- Script Examples -->
		{#each ['song', 'where', 'shoutout'] as example}
			<li>
				<h2 class="font-semibold">
					!{example}

					<SlimButton
						on:click={() => {
							const trigger = example;
							const code = t(`page.chat_bot.commands.examples.${example}.code`);

							commands.push({
								platform: null,
								trigger: trigger,
								response: code,
								type: 'SCRIPT'
							});
							commands = commands;
							save();
						}}
					>
						<LocalizedText key="page.chat_bot.commands.examples.add" />
					</SlimButton>
				</h2>
				<p>
					<LocalizedText key="page.chat_bot.commands.examples.{example}.description" />
				</p>
			</li>
		{/each}
	</ul>
</div>

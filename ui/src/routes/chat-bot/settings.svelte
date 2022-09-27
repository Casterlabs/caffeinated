<script>
	import SelectMenu from '../../components/ui/SelectMenu.svelte';
	import Switch from '../../components/ui/Switch.svelte';
	import TextArea from '../../components/ui/TextArea.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';

	import { hasCasterlabsPlus } from '$lib/app.mjs';
	import { t } from '$lib/translate.mjs';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Chat Bot/Settings');
	const preferences = st || Caffeinated.chatbot.svelte('preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);

	async function setPreferenceItem(name, value) {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			[name]: value
		};
	}
</script>

<ul class="divide-y divide-current text-mauve-6">
	<li class="py-4">
		<SelectMenu
			title="page.chat_bot.settings.sender"
			value={$hasCasterlabsPlus ? $preferences?.chatter : 'SYSTEM'}
			options={{
				SYSTEM: 'page.chat_bot.settings.sender.SYSTEM',
				CLIENT: 'page.chat_bot.settings.sender.CLIENT'
			}}
			disabled={!$hasCasterlabsPlus}
			on:value={({ detail: value }) => setPreferenceItem('chatter', value)}
		/>
	</li>
	<li class="py-4">
		<Switch
			title="page.chat_bot.settings.hide_commands_from_chat"
			description="page.chat_bot.settings.hide_commands_from_chat.description"
			checked={$preferences?.hideCommandsFromChat}
			on:value={({ detail: value }) => setPreferenceItem('hideCommandsFromChat', value)}
		/>
	</li>
	<li class="py-4">
		<Switch
			title="page.chat_bot.settings.hide_timers_from_chat"
			description="page.chat_bot.settings.hide_timers_from_chat.description"
			checked={$preferences?.hideTimersFromChat}
			on:value={({ detail: value }) => setPreferenceItem('hideTimersFromChat', value)}
		/>
	</li>
	<li class="py-4">
		<div class="flex items-center justify-between w-full">
			<div class="flex flex-col">
				<p class="text-sm font-medium text-mauve-12" id="CHATBOT_HIDEFROM-label">
					<LocalizedText key="page.chat_bot.settings.hide_from_chatbots" />
				</p>
				<p class="text-sm text-mauve-11" id="CHATBOT_HIDEFROM-description">
					<LocalizedText key="page.chat_bot.settings.hide_from_chatbots.description" />
				</p>
			</div>
		</div>

		<div class="mt-2">
			<ul class="space-y-2">
				{#each $preferences?.chatbots || [] as chatbot, idx}
					<li class="relative">
						<TextArea
							placeholder="Nightbot"
							rows="1"
							resize={false}
							value={chatbot}
							on:value={({ detail: value }) => {
								const chatbots = $preferences.chatbots;
								chatbots[idx] = value; // Update the value.
								setPreferenceItem('chatbots', chatbots);
							}}
						/>

						<button
							class="absolute inset-y-1 right-1 text-red-500 hover:text-red-600"
							title={t('sr.page.chat_bot.settings.hide_from_chatbots.remove')}
							on:click={() => {
								const chatbots = $preferences.chatbots;
								chatbots.splice(idx, 1);
								setPreferenceItem('chatbots', chatbots);
							}}
						>
							<span class="sr-only">
								<LocalizedText key="sr.page.chat_bot.settings.hide_from_chatbots.remove" />
							</span>
							<icon class="h-5" data-icon="trash" />
						</button>
					</li>
				{/each}
			</ul>

			<button
				class="mt-2 w-full relative flex items-center justify-center rounded-lg border transition border-mauve-4 text-mauve-11 hover:text-mauve-12 hover:shadow-sm hover:border-mauve-7 hover:bg-mauve-2 p-1 focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7"
				title={t('sr.page.chat_bot.settings.hide_from_chatbots.add')}
				on:click={() => {
					const chatbots = $preferences.chatbots;
					chatbots.push(''); // Add a new blank entry.
					setPreferenceItem('chatbots', chatbots);
				}}
			>
				<div>
					<span class="sr-only">
						<LocalizedText key="sr.page.chat_bot.settings.hide_from_chatbots.add" />
					</span>
					<icon data-icon="plus" />
				</div>
			</button>
		</div>
	</li>
</ul>

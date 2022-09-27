<script>
	import SelectMenu from '../../components/ui/SelectMenu.svelte';
	import Switch from '../../components/ui/Switch.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';

	import { hasCasterlabsPlus } from '$lib/app.mjs';
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
	</li>
</ul>

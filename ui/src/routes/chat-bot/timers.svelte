<script>
	import TextArea from '../../components/ui/TextArea.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';
	import NumberInput from '../../components/ui/NumberInput.svelte';

	import { t } from '$lib/translate.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import Debouncer from '$lib/debouncer.mjs';

	const debouncer = new Debouncer();

	const console = createConsole('Chat Bot/Timers');
	const preferences = st || Caffeinated.chatbot.svelte('preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);

	$: timers = $preferences?.timers || [];

	function saveDB() {
		debouncer.debounce(save);
	}

	async function save() {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			timers: timers
		};
	}
</script>

{#if $preferences}
	<LocalizedText key="page.chat_bot.timers.format" slotMapping={['seconds']}>
		<NumberInput slot="0" bind:value={$preferences.timerIntervalSeconds} min={45} on:value={save} />
	</LocalizedText>
{/if}

<ul class="mt-4 space-y-2">
	{#each timers || [] as message, idx}
		<li class="relative">
			<TextArea
				placeholder="page.chat_bot.timers.example"
				rows="2"
				value={message}
				on:value={saveDB}
			/>

			<button
				class="absolute inset-y-1 right-1 text-red-500 hover:text-red-600"
				title={t('sr.page.chat_bot.remove')}
				on:click={() => {
					timers.splice(idx, 1);
					save();
				}}
			>
				<span class="sr-only">
					<LocalizedText key="sr.page.chat_bot.remove" />
				</span>
				<icon class="h-5" data-icon="filled/trash" />
			</button>
		</li>
	{/each}

	<li>
		<button
			class="mt-2 w-full relative flex items-center justify-center rounded-lg border transition border-mauve-4 text-mauve-11 hover:text-mauve-12 hover:shadow-sm hover:border-mauve-7 hover:bg-mauve-2 p-1 focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7"
			title={t('sr.page.chat_bot.settings.hide_from_chatbots.add')}
			on:click={() => {
				timers.push(t('page.chat_bot.timers.example'));
				save();
			}}
		>
			<div>
				<span class="sr-only">
					<LocalizedText key="sr.page.chat_bot.settings.hide_from_chatbots.add" />
				</span>
				<icon data-icon="filled/plus" />
			</div>
		</button>
	</li>
</ul>

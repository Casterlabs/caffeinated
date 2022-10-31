<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import SlimSelectMenu from '../../components/ui/SlimSelectMenu.svelte';
	import TextArea from '../../components/ui/TextArea.svelte';
	import SlimTextArea from '../../components/ui/SlimTextArea.svelte';
	import Container from '../../components/ui/Container.svelte';

	import { t } from '$lib/translate.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import Debouncer from '$lib/debouncer.mjs';

	const debouncer = new Debouncer();

	const PLATFORMS = {
		[null]: 'page.chat_bot.platform.ANY',
		CAFFEINE: 'Caffeine',
		TWITCH: 'Twitch',
		TROVO: 'Trovo',
		GLIMESH: 'Glimesh',
		BRIME: 'Brime'
	};

	const EVENT_TYPES = {
		DONATION: 'page.chat_bot.shouts.DONATION',
		FOLLOW: 'page.chat_bot.shouts.FOLLOW',
		RAID: 'page.chat_bot.shouts.RAID',
		SUBSCRIPTION: 'page.chat_bot.shouts.SUBSCRIPTION'
	};

	const console = createConsole('Chat Bot/Shouts');
	const preferences = st || Caffeinated.chatbot.svelte('preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);

	$: shouts = $preferences?.shouts || [];

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
					key="page.chat_bot.shouts.format"
					slotMapping={['platform', 'action', 'message']}
				>
					<SlimSelectMenu
						slot="0"
						bind:value={shout.platform}
						options={PLATFORMS}
						on:value={saveDB}
					/>

					<SlimSelectMenu
						slot="1"
						bind:value={shout.eventType}
						options={EVENT_TYPES}
						on:value={saveDB}
					/>

					<span slot="2" class="block mt-1.5">
						<TextArea
							placeholder="page.chat_bot.commands.example"
							rows={2}
							bind:value={shout.text}
							on:value={saveDB}
						/>
					</span>
				</LocalizedText>
			</Container>

			<button
				class="absolute top-2 right-1 text-red-500 hover:text-red-600"
				title={t('sr.page.chat_bot.remove')}
				on:click={() => {
					shouts.splice(idx, 1);
					save();
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
			class="mt-2 w-full relative flex items-center justify-center rounded-lg transition hover:text-mauve-12 text-mauve-11 border hover:border-mauve-8 border-mauve-7 hover:bg-mauve-3 bg-mauve-2 py-1 px-2 shadow-sm focus:border-crimson-7 focus:outline-none focus:ring-1 focus:ring-crimson-7"
			title={t('sr.page.chat_bot.commands.add')}
			on:click={() => {
				shouts.push({
					platform: null,
					eventType: 'FOLLOW',
					text: t('page.chat_bot.shouts.example')
				});
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

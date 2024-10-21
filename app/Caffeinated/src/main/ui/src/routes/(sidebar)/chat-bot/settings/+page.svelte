<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import Switch from '$lib/ui/Switch.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';

	import { t } from '$lib/app.mjs';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Chat Bot/Settings');
	const preferences = st || window.svelte('Caffeinated.chatbot', 'preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);
	let state = 0;

	async function setPreferenceItem(name, value) {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			[name]: value
		};
	}
</script>

<ul class="divide-y divide-current text-base-6">
	<li class="py-4">
		<Switch
			title="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chat"
			description="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chat.description"
			checked={$preferences?.hideFromChat}
			on:value={({ detail: value }) => setPreferenceItem('hideFromChat', value)}
		/>
	</li>
	<li class="py-4">
		<div class="flex items-center justify-between w-full">
			<div class="flex flex-col">
				<p class="text-sm font-medium text-base-12" id="CHATBOT_HIDEFROM-label">
					<LocalizedText
						key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots"
					/>
				</p>
				<p class="text-sm text-base-11" id="CHATBOT_HIDEFROM-description">
					<LocalizedText
						key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots.description"
					/>
				</p>
			</div>
		</div>

		<div class="mt-2">
			{#key state}
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
								class="absolute inset-y-1 right-1 text-error hover:opacity-80"
								on:click={() => {
									const chatbots = $preferences.chatbots;
									chatbots.splice(idx, 1);
									setPreferenceItem('chatbots', chatbots);
									state++;
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
				</ul>
			{/key}

			<button
				class="mt-2 w-full relative flex items-center justify-center rounded-lg border transition border-base-5 text-base-11 hover:text-base-12 hover:shadow-sm hover:border-base-7 hover:bg-base-2 p-1 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
				on:click={() => {
					const chatbots = $preferences.chatbots;
					chatbots.push(''); // Add a new blank entry.
					setPreferenceItem('chatbots', chatbots);
					state++;
				}}
			>
				<LocalizedProperty
					key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots.add"
					property="title"
				/>
				<div>
					<span class="sr-only">
						<LocalizedText
							key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots.add"
						/>
					</span>
					<icon data-icon="icon/plus" />
				</div>
			</button>
		</div>
	</li>
	<!-- <li class="py-4">
		<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.settings.store_editor" />
		<div class="h-72 rounded overflow-hidden">
			<CodeInput
				language="json"
				value={JSON.stringify($preferences?.store || {}, null, 2)}
				on:value={({ detail: value }) => setPreferenceItem('store', JSON.parse(value))}
			/>
		</div>
	</li> -->
</ul>

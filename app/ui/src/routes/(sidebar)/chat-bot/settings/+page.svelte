<script lang="ts">
	import { modify, storify } from '$lib/bridge-helper';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconPlus, IconTrash } from '@casterlabs/heroicons-svelte';
	import { Box, Button, Input } from '@casterlabs/ui';

	const chatbotPreferences = storify(AppConfig, 'chatbotPreferences').readable<Awaited<typeof AppConfig.chatbotPreferences>>();

	function addChatbot() {
		const chatbots = $chatbotPreferences?.chatbots || [];
		modify(AppConfig, 'chatbotPreferences', 'chatbots', [...chatbots, '']);
	}

	function removeChatbot(index: number) {
		const chatbots = $chatbotPreferences?.chatbots || [];
		modify(
			AppConfig,
			'chatbotPreferences',
			'chatbots',
			chatbots.filter((_, i) => i !== index)
		);
	}

	function updateChatbot(index: number, value: string) {
		const chatbots = $chatbotPreferences?.chatbots || [];
		const updated = [...chatbots];
		updated[index] = value;
		modify(AppConfig, 'chatbotPreferences', 'chatbots', updated);
	}
</script>

<div class="max-w-4xl mx-auto space-y-8">
	<div class="space-y-4">
		<div class="flex items-center justify-between">
			<div class="flex-1">
				<h3 class="text-base font-semibold text-base-12">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chat" />
				</h3>
				<p class="text-sm text-base-11">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chat.description" />
				</p>
			</div>
			<Input
				type="checkbox"
				checked={$chatbotPreferences?.hideFromChat}
				onchange={(e) => {
					const value = (e.target as HTMLInputElement).checked;
					modify(AppConfig, 'chatbotPreferences', 'hideFromChat', value);
				}}
			/>
		</div>
	</div>

	<div class="space-y-4">
		<div>
			<h3 class="text-base font-semibold text-base-12">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots" />
			</h3>
			<p class="text-sm text-base-11">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots.description" />
			</p>
		</div>

		<div class="space-y-3">
			{#each $chatbotPreferences?.chatbots || [] as chatbot, index}
				<Box class="border border-base-6 rounded-lg p-3 flex items-center gap-4">
					<Input type="text" class="flex-1" value={chatbot} placeholder="Nightbot" onchange={(e) => updateChatbot(index, (e.target as HTMLInputElement).value)} />
					<Button borderless onclick={() => removeChatbot(index)}>
						<IconTrash class="w-5 h-5 text-red-11" theme="solid" />
					</Button>
				</Box>
			{/each}

			<Button onclick={addChatbot} class="w-full p-2 flex items-center justify-center">
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.settings.hide_from_chatbots.add" />
				</span>
				<IconPlus theme="mini" />
			</Button>
		</div>
	</div>
</div>

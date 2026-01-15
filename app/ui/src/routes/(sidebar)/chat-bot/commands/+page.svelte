<script lang="ts">
	import { modify, storify } from '$lib/bridge-helper';

	import CodeEditor from '$lib/layout/widget-settings/CodeEditor.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconPlus, IconTrash } from '@casterlabs/heroicons-svelte';
	import { Box, Button, Input, Select } from '@casterlabs/ui';

	const chatbotPreferences = storify(AppConfig, 'chatbotPreferences').readable<Awaited<typeof AppConfig.chatbotPreferences>>();

	const TRIGGER_TYPES = {
		COMMAND: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.trigger_type.COMMAND',
		CONTAINS: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.trigger_type.CONTAINS',
		ALWAYS: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.trigger_type.ALWAYS'
	};

	const RESPONSE_ACTIONS = {
		REPLY_WITH: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.response_action.REPLY_WITH',
		EXECUTE: 'co.casterlabs.caffeinated.app.page.chat_bot.commands.response_action.EXECUTE'
	};

	function addCommand() {
		const commands = $chatbotPreferences?.commands || [];
		modify(AppConfig, 'chatbotPreferences', 'commands', [
			...commands,
			{
				triggerType: 'COMMAND',
				trigger: 'hello',
				responseAction: 'REPLY_WITH',
				response: 'Hello!',
				platform: null
			}
		]);
	}

	function removeCommand(index: number) {
		const commands = $chatbotPreferences?.commands || [];
		modify(
			AppConfig,
			'chatbotPreferences',
			'commands',
			commands.filter((_, i) => i !== index)
		);
	}

	function updateCommand(index: number, field: string, value: any) {
		const commands = $chatbotPreferences?.commands || [];
		const updated = [...commands];
		updated[index] = { ...updated[index], [field]: value };
		modify(AppConfig, 'chatbotPreferences', 'commands', updated);
	}
</script>

<div class="max-w-4xl mx-auto">
	<div class="space-y-6">
		{#each $chatbotPreferences?.commands || [] as command, index}
			<Box class="p-3">
				<div class="flex items-start justify-between gap-4 mb-4">
					<div class="flex flex-wrap items-center gap-2 text-base text-base-12">
						{#snippet platform()}
							<Select
								class="inline-block w-auto min-w-[140px]"
								onchange={(e) => {
									let value: string | null = (e.target as HTMLSelectElement).value;
									if (value === '') {
										value = null;
									}
									updateCommand(index, 'platform', value);
								}}
							>
								<option value={''} selected={!command.platform}>
									<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.platform.ANY" />
								</option>

								{#await AppAuth.AUTHENTICATABLE then AUTHENTICATABLE_PLATFORMS}
									{#each AUTHENTICATABLE_PLATFORMS as platform}
										<option value={platform} selected={platform == command.platform}>
											<LocalizedText key="co.casterlabs.caffeinated.app.platform.{platform}" />
										</option>
									{/each}
								{/await}
							</Select>
						{/snippet}

						{#snippet trigger_type()}
							<Select
								class="inline-block w-auto min-w-[160px]"
								value={command.triggerType}
								onchange={(e) => updateCommand(index, 'triggerType', (e.target as HTMLSelectElement).value)}
							>
								{#each Object.entries(TRIGGER_TYPES) as [key, label]}
									<option value={key} selected={command.triggerType === key}>
										<LocalizedText key={label} />
									</option>
								{/each}
							</Select>
						{/snippet}

						{#snippet trigger()}
							{#if command.triggerType !== 'ALWAYS'}
								<Input
									type="text"
									class="inline-block w-auto min-w-[120px] max-w-[200px]"
									value={command.trigger}
									placeholder="example"
									onchange={(e) => updateCommand(index, 'trigger', (e.target as HTMLInputElement).value)}
								/>
							{/if}
						{/snippet}

						{#snippet response_action()}
							<Select
								class="inline-block w-auto min-w-[120px]"
								value={command.responseAction}
								onchange={(e) => updateCommand(index, 'responseAction', (e.target as HTMLSelectElement).value)}
							>
								{#each Object.entries(RESPONSE_ACTIONS) as [key, label]}
									<option value={key} selected={command.responseAction === key}>
										<LocalizedText key={label} />
									</option>
								{/each}
							</Select>
						{/snippet}

						{#snippet response()}
							<!-- placeholder for response -->
						{/snippet}

						<LocalizedText
							key="co.casterlabs.caffeinated.app.page.chat_bot.commands.format.{command.triggerType}"
							components={{ platform, trigger_type, trigger, response_action, response }}
						/>
					</div>
					<Button borderless onclick={() => removeCommand(index)}>
						<IconTrash class="w-5 h-5 text-red-11" theme="solid" />
					</Button>
				</div>

				{#if command.responseAction === 'EXECUTE'}
					<div class="w-full h-72 text-left border border-base-8 rounded-sm relative overflow-hidden">
						<CodeEditor language="javascript" value={command.response} onchange={(value) => updateCommand(index, 'response', value)} />
					</div>
				{:else}
					<Input
						type="text"
						class="w-full"
						value={command.response}
						placeholder="Hello!"
						onchange={(e) => updateCommand(index, 'response', (e.target as HTMLInputElement).value)}
					/>
				{/if}
			</Box>
		{/each}

		<Button onclick={addCommand} class="w-full p-2 flex items-center justify-center">
			<span class="sr-only">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.commands.add" />
			</span>
			<IconPlus theme="mini" />
		</Button>
	</div>
</div>

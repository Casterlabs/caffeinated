<script lang="ts">
	import { modify, storify } from '$lib/bridge-helper';

	import CodeEditor from '$lib/layout/widget-settings/CodeEditor.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconPlus, IconTrash } from '@casterlabs/heroicons-svelte';
	import { Box, Button, Input, Select, TextArea } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	const ACTIONS = {
		REPLY_WITH: 'co.casterlabs.caffeinated.app.page.chat_bot.shouts.response_action.REPLY_WITH',
		EXECUTE: 'co.casterlabs.caffeinated.app.page.chat_bot.shouts.response_action.EXECUTE'
	};

	const chatbotPreferences = storify(AppConfig, 'chatbotPreferences').readable<Awaited<typeof AppConfig.chatbotPreferences>>();

	let supportedShoutEvents: Awaited<typeof AppChatbot.supportedShoutEvents> = $state([]);

	onMount(async () => {
		supportedShoutEvents = await AppChatbot.supportedShoutEvents;
	});

	function addShout() {
		const shouts = $chatbotPreferences?.shouts || [];
		modify(AppConfig, 'chatbotPreferences', 'shouts', [
			...shouts,
			{
				eventType: 'FOLLOW',
				responseAction: 'REPLY_WITH',
				response: '',
				platform: null
			}
		]);
	}

	function removeShout(index: number) {
		const shouts = $chatbotPreferences?.shouts || [];
		modify(
			AppConfig,
			'chatbotPreferences',
			'shouts',
			shouts.filter((_, i) => i !== index)
		);
	}

	function updateShout(index: number, field: string, value: any) {
		const shouts = $chatbotPreferences?.shouts || [];
		const updated = [...shouts];
		updated[index] = { ...updated[index], [field]: value };
		modify(AppConfig, 'chatbotPreferences', 'shouts', updated);
	}
</script>

<div class="max-w-4xl mx-auto">
	<div class="space-y-6">
		{#each $chatbotPreferences?.shouts || [] as shout, index}
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
									updateShout(index, 'platform', value);
								}}
							>
								<option value={''} selected={!shout.platform}>
									<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.platform.ANY" />
								</option>

								{#await AppAuth.AUTHENTICATABLE then AUTHENTICATABLE_PLATFORMS}
									{#each AUTHENTICATABLE_PLATFORMS as platform}
										<option value={platform} selected={platform == shout.platform}>
											<LocalizedText key="co.casterlabs.caffeinated.app.platform.{platform}" />
										</option>
									{/each}
								{/await}
							</Select>
						{/snippet}

						{#snippet action()}
							<Select
								class="inline-block w-auto min-w-[160px]"
								value={shout.eventType}
								onchange={(e) => {
									updateShout(index, 'eventType', (e.target as HTMLSelectElement).value);
								}}
							>
								{#each supportedShoutEvents as eventType}
									<option value={eventType} selected={eventType === shout.eventType}>
										<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.shouts.{eventType}" />
									</option>
								{/each}
							</Select>
						{/snippet}

						{#snippet response_action()}
							<Select
								class="inline-block w-auto min-w-[120px]"
								value={shout.responseAction}
								onchange={(e) => {
									updateShout(index, 'responseAction', (e.target as HTMLSelectElement).value);
								}}
							>
								{#each Object.entries(ACTIONS) as [key, label]}
									<option value={key} selected={shout.responseAction === key}>
										<LocalizedText key={label} />
									</option>
								{/each}
							</Select>
						{/snippet}

						{#snippet response()}
							<!-- placeholder -->
						{/snippet}

						<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.shouts.format" components={{ platform, action, response_action, response }} />
					</div>
					<Button borderless onclick={() => removeShout(index)}>
						<IconTrash class="w-5 h-5 text-red-11" theme="solid" />
					</Button>
				</div>

				{#if shout.responseAction === 'EXECUTE'}
					<div class="w-full h-72 text-left border border-base-8 rounded-sm relative overflow-hidden">
						<CodeEditor
							language="javascript"
							value={shout.response}
							onchange={(value) => {
								updateShout(index, 'response', value);
							}}
						/>
					</div>
				{:else}
					<Input
						type="text"
						class="w-full"
						value={shout.response}
						placeholder={'Thank you for the follow @${event.sender.displayname}!'}
						onchange={(e) => {
							updateShout(index, 'response', (e.target as HTMLInputElement).value);
						}}
					/>
				{/if}
			</Box>
		{/each}

		<Button onclick={addShout} class="w-full p-2 flex items-center justify-center">
			<span class="sr-only">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.shouts.add" />
			</span>
			<IconPlus theme="mini" />
		</Button>
	</div>
</div>

<script lang="ts">
	import { modify, storify } from '$lib/bridge-helper';
	import { render } from '$lib/locale/locale';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconPlus, IconTrash } from '@casterlabs/heroicons-svelte';
	import { Box, Button, Input } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	const chatbotPreferences = storify(AppConfig, 'chatbotPreferences').readable<Awaited<typeof AppConfig.chatbotPreferences>>();
	const nextMessageAt = storify(AppChatbot, 'nextMessageAt').readable<Awaited<typeof AppChatbot.nextMessageAt>>();

	let nextMessageString = $state('');

	function addTimer() {
		const timers = $chatbotPreferences?.timers || [];
		modify(AppConfig, 'chatbotPreferences', 'timers', [...timers, '']);
	}

	function removeTimer(index: number) {
		const timers = $chatbotPreferences?.timers || [];
		modify(
			AppConfig,
			'chatbotPreferences',
			'timers',
			timers.filter((_, i) => i !== index)
		);
	}

	function updateTimer(index: number, value: string) {
		const timers = $chatbotPreferences?.timers || [];
		const updated = [...timers];
		updated[index] = value;
		modify(AppConfig, 'chatbotPreferences', 'timers', updated);
	}

	onMount(() => {
		const id = setInterval(() => {
			const next = $nextMessageAt;
			if (next === null || next === -1) {
				nextMessageString = render('co.casterlabs.caffeinated.app.page.chat_bot.timers.next.never');
			} else if (next <= Date.now()) {
				nextMessageString = render('co.casterlabs.caffeinated.app.page.chat_bot.timers.next.now');
			} else {
				const seconds = Math.floor((next - Date.now()) / 1000).toString();
				nextMessageString = render('co.casterlabs.caffeinated.app.page.chat_bot.timers.next.seconds', { seconds });
			}
		}, 1000);
		return () => clearInterval(id);
	});
</script>

<div class="max-w-4xl mx-auto">
	<div class="mb-6">
		<div class="flex flex-wrap items-center gap-2 text-base text-base-12">
			{#snippet seconds()}
				<Input
					type="number"
					class="inline-block w-24"
					min="45"
					max="3600"
					value={$chatbotPreferences?.timerIntervalSeconds}
					onchange={(e) => {
						const value = parseInt((e.target as HTMLInputElement).value);
						modify(AppConfig, 'chatbotPreferences', 'timerIntervalSeconds', value);
					}}
				/>
			{/snippet}
			<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.timers.format" components={{ seconds }} />
		</div>
	</div>

	<div class="space-y-4">
		{#each $chatbotPreferences?.timers || [] as timer, index}
			<Box class="border border-base-6 rounded-lg p-3 flex items-center gap-4">
				<Input type="text" class="flex-1" value={timer} placeholder="I love Casterlabs!" onchange={(e) => updateTimer(index, (e.target as HTMLInputElement).value)} />
				<Button borderless onclick={() => removeTimer(index)}>
					<IconTrash class="w-5 h-5 text-red-11" theme="solid" />
				</Button>
			</Box>
		{/each}

		<Button onclick={addTimer} class="w-full p-2 flex items-center justify-center">
			<span class="sr-only">
				<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.timers.add" />
			</span>
			<IconPlus theme="mini" />
		</Button>
	</div>

	<div class="mt-6">
		<p class="text-base text-base-12">
			{nextMessageString}
		</p>
		<p class="text-sm text-base-11">
			<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.timers.format.disclaimer" />
		</p>
	</div>
</div>

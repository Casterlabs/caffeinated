<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import LocalizedProperty from '$lib/LocalizedProperty.svelte';
	import TextArea from '$lib/ui/TextArea.svelte';
	import NumberInput from '$lib/ui/NumberInput.svelte';

	import { t } from '$lib/app.mjs';
	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Chat Bot/Timers');

	const nextMessageAt = st || window.svelte('Caffeinated.chatbot', 'nextMessageAt');
	let nextMessageIn_seconds = '';

	let timers = [];
	let timerInterval_seconds = 0;

	async function save() {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			timers: timers,
			timerIntervalSeconds: timerInterval_seconds
		};
	}

	onMount(() => {
		Caffeinated.chatbot.preferences.then((prefs) => {
			console.debug('Chat Bot Preferences:', prefs);
			timers = prefs.timers;
			timerInterval_seconds = prefs.timerIntervalSeconds;
		});

		let task = setInterval(() => {
			const next = $nextMessageAt;
			if (next > 0) {
				nextMessageIn_seconds = ((next - Date.now()) / 1000).toFixed(0);
			} else {
				nextMessageIn_seconds = null;
			}
		}, 1000);
		return () => clearInterval(task);
	});
</script>

<LocalizedText
	key="co.casterlabs.caffeinated.app.page.chat_bot.timers.format"
	slotMapping={['seconds']}
>
	<span slot="0" class="inline-block w-20">
		<NumberInput
			bind:value={timerInterval_seconds}
			on:value={() => {
				if (timerInterval_seconds < 45) {
					timerInterval_seconds = 45; // Min.
				}
				save();
			}}
		/>
	</span>
</LocalizedText>
<br />

<ul class="mt-4 space-y-2">
	{#each timers || [] as message, idx}
		<li class="relative">
			<TextArea
				placeholder="co.casterlabs.caffeinated.app.page.chat_bot.timers.example"
				rows="2"
				bind:value={message}
				on:value={save}
			/>

			<button
				class="absolute inset-y-1 right-1 text-error hover:opacity-80"
				on:click={() => {
					timers.splice(idx, 1);
					save();
					timers = timers;
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

	<li>
		<button
			class="mt-2 w-full relative flex items-center justify-center rounded-lg transition hover:text-base-12 text-base-11 border hover:border-base-8 border-base-7 hover:bg-base-3 bg-base-2 py-1 px-2 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7"
			on:click={async () => {
				timers.push(await t('co.casterlabs.caffeinated.app.page.chat_bot.timers.example'));
				save();
				timers = timers;
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
	</li>
</ul>
<span class="text-xs">
	<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.timers.format.disclaimer" />
</span>
<br />

<br />
{#if nextMessageIn_seconds}
	{#if nextMessageIn_seconds > 0}
		<LocalizedText
			key="co.casterlabs.caffeinated.app.page.chat_bot.timers.next.seconds"
			opts={{ seconds: nextMessageIn_seconds }}
		/>
	{:else}
		<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.timers.next.now" />
	{/if}
{:else}
	<LocalizedText key="co.casterlabs.caffeinated.app.page.chat_bot.timers.next.never" />
{/if}

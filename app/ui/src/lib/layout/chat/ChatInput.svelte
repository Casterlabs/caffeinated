<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';
	import { type KoiStatics, type RichMessageEvent, type UPID, type User, type UserPlatform, type UserUpdateEvent } from '$lib/koi';
	import { render } from '$lib/locale/locale';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	// import { openMenu } from '../menus/menu';

	import PlatformIcon from '../PlatformIcon.svelte';
	import { IconCog6Tooth } from '@casterlabs/heroicons-svelte';
	import { IconArrowUturnLeft } from '@casterlabs/heroicons-svelte';

	import { onMount } from 'svelte';

	interface Props {
		uiEvents: EventHandler;
	}

	let { uiEvents }: Props = $props();

	// svelte-ignore non_reactive_update
	let textInputElement: HTMLInputElement;

	let persistentEvents: KoiStatics = $state({ userStates: {} } as any);

	let replyTarget: RichMessageEvent | null = $state(null); // null = no target
	let textInput = $state('');

	let sendTarget: User | null = $state(null); // null = all

	onMount(() => {
		function recalculateSelectedTarget() {
			const hasMultiplePlatforms = Object.keys(persistentEvents.userStates).length > 1;

			if (hasMultiplePlatforms) {
				if (!sendTarget) {
					return; // `null` (a.k.a "All") is always a valid target.
				}

				for (const state of Object.values(persistentEvents.userStates)) {
					if (state.streamer.UPID == sendTarget.UPID) {
						// Our selected target is still valid!
						return;
					}
				}
			}

			// We need to reset our selected target since it's no longer valid (or we don't have any accounts anymore).
			sendTarget = Object.values(persistentEvents.userStates)[0]?.streamer || null;
		}

		recalculateSelectedTarget();

		return Koi.on('koi_statics', (statics: KoiStatics) => {
			persistentEvents = statics;
			recalculateSelectedTarget();
		});
	});

	onMount(() =>
		uiEvents.on('x-start-reply', (event: RichMessageEvent | null) => {
			replyTarget = event;
			textInputElement.focus();
			textInputElement.click();
		})
	);

	onMount(() =>
		uiEvents.on('x-sendtarget', (st: User | null) => {
			sendTarget = st;
		})
	);

	onMount(async () => {
		const statics = await Koi.statics();
		Koi.broadcast('koi_statics', statics);
	});

	function send(target: UserPlatform) {
		Koi.sendChatMessage(target, textInput, replyTarget?.id || null);

		if (replyTarget) {
			uiEvents.broadcast('x-jump-bottom');
		}

		textInput = '';
		replyTarget = null;
	}

	function submit() {
		try {
			if (textInput.length == 0) return;

			if (replyTarget) {
				// We're in reply mode, we already know where to send the event.
				send(replyTarget.streamer.platform);
				return;
			}

			if (sendTarget) {
				send(sendTarget.platform);
				return;
			}

			// Send to all.
			const textInputCopy = textInput;
			for (const userUpdate of Object.values(persistentEvents.userStates)) {
				if (!userUpdate) return; // Check to see if the account is valid
				textInput = textInputCopy;
				send(userUpdate.streamer.platform);
			}
		} finally {
			setTimeout(() => {
				// Next tick.
				textInputElement.focus();
				textInputElement.click();
			}, 2);
		}
	}
</script>

{#if true}
	{@const placeholder = replyTarget
		? render('co.casterlabs.caffeinated.app.docks.chat.viewer.send_message.reply.placeholder', { name: replyTarget.sender.username })
		: sendTarget
			? render('co.casterlabs.caffeinated.app.docks.chat.viewer.send_message.platform.placeholder', {
					username: sendTarget.username,
					platform: render(`co.casterlabs.caffeinated.app.platform.${sendTarget.platform}`)
				})
			: render('co.casterlabs.caffeinated.app.docks.chat.viewer.send_message.all.placeholder')}

	<form class="relative flex h-10 flex-row" autocomplete="off" onsubmit={submit}>
		<button
			class="text-base-11 bg-base-3 border-base-7 hover:bg-base-5 active:bg-base-5 hover:border-base-8 active:border-base-6 focus:border-base-8 border-r-0 rounded-l-[var(--clui-radius)] flex h-full items-center justify-center border min-w-12 py-1 px-2 text-sm"
			onclick={() => {
				if (replyTarget) {
					uiEvents.broadcast('x-reply-modal', replyTarget);
				} else {
					uiEvents.broadcast('x-sendtarget-modal');
				}
			}}
			type="button"
			title={placeholder}
		>
			{#if replyTarget}
				<IconArrowUturnLeft theme="micro" />
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.reply_menu.open" />
				</span>
			{:else if sendTarget}
				<span aria-hidden="true">
					<PlatformIcon platform={sendTarget.platform} color />
					{sendTarget.username}
				</span>
			{:else}
				<span aria-hidden="true">
					<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.send_message.all" />
				</span>
			{/if}
		</button>

		<div class="flex-1 relative">
			<input
				bind:this={textInputElement}
				type="text"
				class="text-base-11 bg-base-3 border-base-7 hover:bg-base-5 active:bg-base-5 hover:border-base-8 active:border-base-6 focus:border-base-8 h-full w-full border px-3 py-1 text-sm focus:outline-none"
				{placeholder}
				autocomplete="off"
				bind:value={textInput}
				onkeydown={(e) => {
					if (e.key === 'Escape') {
						replyTarget = null;
					}
				}}
			/>

			<button
				class="absolute inset-y-2 right-2 flex items-center"
				onclick={() => uiEvents.broadcast('x-preferences-modal')}
				type="button"
				title={render('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.title')}
			>
				<IconCog6Tooth theme="outline" />
			</button>
		</div>

		<button
			class="text-base-11 bg-base-3 border-base-7 border-l-0 rounded-r-[var(--clui-radius)] flex h-full items-center justify-center border px-3 py-1 text-sm"
			class:hover:border-base-8={textInput.length > 0}
			class:active:border-base-8={textInput.length > 0}
			class:focus:border-base-8={textInput.length > 0}
			class:text-base-12={textInput.length > 0}
			disabled={textInput.length == 0}
			type="submit"
		>
			{#if replyTarget}
				<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.send_message.reply" />
			{:else}
				<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.send_message" />
			{/if}
		</button>
	</form>
{/if}

<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type EventHandler from '$lib/event-handler';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import Modal from '../Modal.svelte';
	import PlatformIcon from '../PlatformIcon.svelte';
	import { Button } from '@casterlabs/ui';

	interface Props {
		onclose: () => void;
		uiEvents: EventHandler;
	}

	let { onclose, uiEvents }: Props = $props();
</script>

{#snippet title()}
	<div class="flex items-center justify-center">
		<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.send.modal.title" />
	</div>
{/snippet}

<Modal {title} {onclose}>
	<ul class="space-y-1">
		{#await Koi.statics() then statics}
			{#each Object.values(statics.userStates) as { streamer }}
				{@const displaynameDiffers = streamer.displayname.toLowerCase() !== streamer.username.toLowerCase()}

				<li>
					<Button
						class="w-full flex justify-center items-center"
						style="padding: 0.5rem;"
						onclick={() => {
							uiEvents.broadcast('x-sendtarget', streamer);
							onclose();
						}}
					>
						<div class="flex-1">
							<PlatformIcon platform={streamer.platform} color />
							<span class="sr-only">
								<LocalizedText key="co.casterlabs.caffeinated.app.platform.{streamer.platform}" />
							</span>
							{streamer.displayname}
							{#if displaynameDiffers}
								<span class="text-sm text-base-11">({streamer.username})</span>
							{/if}
						</div>
					</Button>
				</li>
			{/each}

			<li>
				<Button
					class="w-full flex justify-center items-center"
					style="padding: 0.5rem;"
					onclick={() => {
						uiEvents.broadcast('x-sendtarget', null);
						onclose();
					}}
				>
					<LocalizedText key="co.casterlabs.caffeinated.app.docks.chat.viewer.send.modal.all" />
				</Button>
			</li>
		{/await}
	</ul>
</Modal>

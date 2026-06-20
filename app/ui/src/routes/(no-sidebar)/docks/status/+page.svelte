<script lang="ts">
	import { Koi } from '$lib/app-shim';
	import type { KoiStatics } from '$lib/koi';
	import { render } from '$lib/locale/locale';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconCheck, IconExclamationTriangle, IconEye, IconStar, IconUsers, IconXMark } from '@casterlabs/heroicons-svelte';

	import { type Snippet, onMount } from 'svelte';

	let statics: KoiStatics | null = $state(null);

	let isHidingViewers = $state(false);
	let isHidingFollowers = $state(false);
	let isHidingSubscribers = $state(false);

	let connectionState: 'CONNECTED' | 'DISCONNECTED' | 'PARTIALLY_CONNECTED' = $derived.by(() => {
		if (!statics) return 'DISCONNECTED';

		let connectedCount = 0;
		let disconnectedCount = 0;
		for (const states of Object.values(statics.connectionStates)) {
			if (Object.keys(states).length == 0) {
				disconnectedCount++;
				continue;
			}

			for (const state of Object.values(states)) {
				if (state == 'DISCONNECTED') {
					disconnectedCount++;
				} else {
					connectedCount++;
				}
			}
		}
		if (disconnectedCount > 0 && connectedCount > 0) {
			return 'PARTIALLY_CONNECTED';
		} else if (disconnectedCount > 0) {
			return 'DISCONNECTED';
		} else {
			return 'CONNECTED';
		}
	});

	let disconnectedConnections: string[] = $derived.by(() => {
		if (!statics) return [];

		let connections: string[] = [];
		for (const [connectionName, states] of Object.entries(statics.connectionStates)) {
			if (Object.keys(states).length == 0) {
				connections.push(connectionName);
				continue;
			}

			for (const [key, state] of Object.entries(states)) {
				if (state == 'DISCONNECTED') {
					connections.push(key);
				}
			}
		}
		return connections;
	});

	let isAnyLive = $derived.by(() => {
		if (!statics) return false;
		for (const status of Object.values(statics.streamStates)) {
			if (status.is_live) {
				return true;
			}
		}
		return false;
	});

	let totalViewerCount = $derived.by(() => {
		if (!statics) return 0;
		let count = 0;
		for (const c of Object.values(statics.viewerCounts)) {
			if (c > 0) count += c;
		}
		return count;
	});

	let totalFollowerCount = $derived.by(() => {
		if (!statics) return 0;
		let count = 0;
		for (const u of Object.values(statics.userStates)) {
			if (u.streamer.followers_count > 0) count += u.streamer.followers_count;
		}
		return count;
	});

	let totalSubscriberCount = $derived.by(() => {
		if (!statics) return 0;
		let count = 0;
		for (const u of Object.values(statics.userStates)) {
			if (u.streamer.subscriber_count > 0) count += u.streamer.subscriber_count;
		}
		return count;
	});

	Koi.on('koi_statics', (s: KoiStatics) => {
		statics = s;
	});

	onMount(async () => {
		statics = await Koi.statics();
	});
</script>

{#snippet block(title: string | null, content: Snippet, onclick?: () => void)}
	<button class="py-1 px-2 bg-base-4 w-fit h-full text-sm flex flex-row space-x-1 items-center rounded-xs" title={title ? render(title) : null} {onclick}>
		{@render content()}
	</button>
{/snippet}

{#snippet connections()}
	<div
		class="py-1 px-2 bg-base-4 w-fit h-full text-sm flex flex-row space-x-1 items-center rounded-xs"
		title={disconnectedConnections.length > 0
			? render('co.casterlabs.caffeinated.app.docks.status.disconnected', { items: disconnectedConnections.join(', \n') })
			: render('co.casterlabs.caffeinated.app.docks.status.healthy')}
	>
		{#if connectionState == 'CONNECTED'}
			<IconCheck theme="micro" class="h-5 w-4 inline-block text-green-500" />
		{:else if connectionState == 'PARTIALLY_CONNECTED'}
			<IconExclamationTriangle theme="micro" class="h-5 w-4 inline-block text-yellow-500" />
		{:else}
			<IconXMark theme="micro" class="h-5 w-4 inline-block text-red-500" />
		{/if}
	</div>
{/snippet}

{#snippet onAir()}
	{#if isAnyLive}
		<div class="livedot inline-block"></div>
		<span class="text-base-12">
			<LocalizedText key="co.casterlabs.caffeinated.app.docks.status.live" />
		</span>
	{:else}
		<div class="offlinedot inline-block bg-base-11"></div>
		<span class="text-base-11">
			<LocalizedText key="co.casterlabs.caffeinated.app.docks.status.offline" />
		</span>
	{/if}
{/snippet}

{#snippet viewerCount()}
	<IconEye theme="micro" class="h-5 w-4 inline-block {isHidingViewers ? 'text-base-11' : ''}" />

	{#if isHidingViewers}
		<span class="text-base-11">--</span>
	{:else}
		<span>{totalViewerCount.toLocaleString()}</span>
	{/if}
{/snippet}

{#snippet followerCount()}
	<IconUsers theme="micro" class="h-5 w-4 inline-block {isHidingFollowers ? 'text-base-11' : ''}" />

	{#if isHidingFollowers}
		<span class="text-base-11">--</span>
	{:else}
		<span>{totalFollowerCount.toLocaleString()}</span>
	{/if}
{/snippet}

{#snippet subscriberCount()}
	<IconStar theme="micro" class="h-5 w-4 inline-block {isHidingSubscribers ? 'text-base-11' : ''}" />

	{#if isHidingSubscribers}
		<span class="text-base-11">--</span>
	{:else}
		<span>{totalSubscriberCount.toLocaleString()}</span>
	{/if}
{/snippet}

<div class="flex flex-row justify-center items-center gap-2 p-2 h-fit">
	{#if statics}
		{@render connections()}
		{@render block(null, onAir)}
		{@render block('co.casterlabs.caffeinated.app.docks.status.viewers', viewerCount, () => (isHidingViewers = !isHidingViewers))}
		{@render block('co.casterlabs.caffeinated.app.docks.status.followers', followerCount, () => (isHidingFollowers = !isHidingFollowers))}
		{@render block('co.casterlabs.caffeinated.app.docks.status.subscribers', subscriberCount, () => (isHidingSubscribers = !isHidingSubscribers))}
	{/if}
</div>

<style>
	.offlinedot {
		width: 10px;
		aspect-ratio: 1;
		border-radius: 50%;
	}
	.livedot {
		width: 10px;
		aspect-ratio: 1;
		border-radius: 50%;
		background: #d00;
		box-shadow: 0 0 0 0 #d004;
		animation: pulsateanim 2s infinite;
	}
	@keyframes pulsateanim {
		100% {
			box-shadow: 0 0 0 15px #d000;
		}
	}
</style>

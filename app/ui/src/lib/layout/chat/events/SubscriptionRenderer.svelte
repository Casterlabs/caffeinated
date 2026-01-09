<script lang="ts">
	import type { SubscriptionEvent } from '$lib/koi';

	import UsernameRenderer from '../UsernameRenderer.svelte';

	interface Props {
		event: SubscriptionEvent;
	}

	let { event }: Props = $props();

	let isGift = $derived(event.sub_type.includes('GIFT'));
</script>

{#if isGift}
	{@const subscriptionTierString = {
		UNKNOWN: '', // Display nothing.
		TWITCH_PRIME: 'Twitch Prime',
		TIER_1: 'Tier 1',
		TIER_2: 'Tier 2',
		TIER_3: 'Tier 3',
		TIER_4: 'Tier 4',
		TIER_5: 'Tier 5'
	}[event.sub_level]}

	<UsernameRenderer user={event.subscriber} />
	just gifted <UsernameRenderer user={event.gift_recipient} /> a {subscriptionTierString} subscription!
{:else}
	{@const subscriptionTypeString = {
		SUB: 'subscribed',
		RESUB: 'resubscribed',
		SUBGIFT: '<invalid>',
		RESUBGIFT: '<invalid>',
		ANONSUBGIFT: '<invalid>',
		ANONRESUBGIFT: '<invalid>'
	}[event.sub_type]}

	{@const subscriptionTierString = {
		UNKNOWN: '', // Display nothing.
		TWITCH_PRIME: ' with Twitch Prime',
		TIER_1: ' at Tier 1',
		TIER_2: ' at Tier 2',
		TIER_3: ' at Tier 3',
		TIER_4: ' at Tier 4',
		TIER_5: ' at Tier 5'
	}[event.sub_level]}

	<UsernameRenderer user={event.subscriber} />

	just

	{#if event.months_purchased > 1}
		{subscriptionTypeString}{subscriptionTierString}
		for <b>{event.months_purchased}</b> months!
	{:else}
		{subscriptionTypeString}{subscriptionTierString}!
	{/if}

	{#if event.months_streak > 1}
		They're on a <b>{event.months_streak}</b> month streak!
	{/if}
{/if}

<style>
	b {
		color: var(--color-accent-9);
		font-weight: 600;
	}
</style>

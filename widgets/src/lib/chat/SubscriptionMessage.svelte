<script>
	export let event;
</script>

<!-- TODO Localize -->

{#if event.sub_type == 'SUB'}
	<b style:color={event.subscriber.color || 'red'}>{event.subscriber.displayname}</b> just
	subscribed for <b>{event.months}</b> months
{:else if event.sub_type == 'RESUB'}
	<b style:color={event.subscriber.color || 'red'}>{event.subscriber.displayname}</b> just
	resubscribed for <b>{event.months}</b> months
{:else if event.sub_type == 'SUBGIFT'}
	<b style:color={event.subscriber.color || 'red'}>{event.subscriber.displayname}</b> just gifted
	<b style:color={event.gift_recipient.color || 'red'}>{event.gift_recipient.displayname}</b>
	a
	<b>{event.months}</b> month subscription
{:else if event.sub_type == 'ANONSUBGIFT'}
	Anonymous just gifted <b style:color={event.gift_recipient.color || 'red'}
		>{event.gift_recipient.displayname}</b
	>
	a <b>{event.months}</b> month subscription
{/if}

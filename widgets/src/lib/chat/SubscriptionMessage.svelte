<script>
	export let event;
</script>

<!-- TODO Localize -->

{#if event.sub_type == 'SUB'}
	<b style:--user-color={event.subscriber.color || 'red'}>
		<icon
			class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
			data-icon="service/{event.streamer.platform.toLowerCase()}"
			alt=""
		/>
		{event.subscriber.displayname}
	</b>
	just subscribed for
	<b style:--user-color={event.subscriber.color || 'red'}>{event.months}</b>
	months
{:else if event.sub_type == 'RESUB'}
	<b style:--user-color={event.subscriber.color || 'red'}>
		<icon
			class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
			data-icon="service/{event.streamer.platform.toLowerCase()}"
			alt=""
		/>
		{event.subscriber.displayname}
	</b>
	just resubscribed for
	<b style:--user-color={event.subscriber.color || 'red'}>{event.months}</b>
	months
{:else if event.sub_type == 'SUBGIFT'}
	<b style:--user-color={event.subscriber.color || 'red'}>
		<icon
			class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
			data-icon="service/{event.streamer.platform.toLowerCase()}"
			alt=""
		/>
		{event.subscriber.displayname}
	</b>
	just gifted
	<b style:--user-color={event.gift_recipient.color || 'red'}>{event.gift_recipient.displayname}</b>
	a
	<b style:--user-color={event.subscriber.color || 'red'}>{event.months}</b>
	month subscription
{:else if event.sub_type == 'ANONSUBGIFT'}
	<b style:--user-color="red">
		<icon
			class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
			data-icon="service/{event.streamer.platform.toLowerCase()}"
			alt=""
		/>
		Anonymous
	</b>
	just gifted
	<b style:--user-color={event.gift_recipient.color || 'red'}>{event.gift_recipient.displayname}</b>
	a
	<b style:--user-color="red">{event.months}</b>
	month subscription
{/if}

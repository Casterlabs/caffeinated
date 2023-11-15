<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import User from './User.svelte';

	import streamingServices from '$lib/streamingServices.mjs';

	export let event;
	export let onContextMenuAction;
</script>

<LocalizedText
	key="co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.SUBSCRIPTION"
	opts={{
		months: event.months,
		months_purchased: event.months_purchased,
		months_streak: event.months_streak,
		level: event.sub_level,
		type: event.sub_type
	}}
	slotMapping={['name', 'recipient', 'months', 'months_purchased', 'months_streak']}
>
	<User slot="0" user={event.subscriber} />
	<User slot="1" user={event.gift_recipient} />

	<b
		slot="2"
		style:--platform-color={streamingServices[event.streamer.platform]?.color}
		style:--user-color={event.gift_recipient?.color || event.subscriber?.color}
	>
		{event.months}
	</b>
	<b
		slot="3"
		style:--platform-color={streamingServices[event.streamer.platform]?.color}
		style:--user-color={event.gift_recipient?.color || event.subscriber?.color}
	>
		{event.months_purchased}
	</b>
	<b
		slot="4"
		style:--platform-color={streamingServices[event.streamer.platform]?.color}
		style:--user-color={event.gift_recipient?.color || event.subscriber?.color}
	>
		{event.months_streak}
	</b>
</LocalizedText>

<script>
	export let event;
</script>

<!-- TODO Localize -->

<b style:--user-color={event.host.color || 'red'}>
	<icon
		class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
		data-icon="service/{event.streamer.platform.toLowerCase()}"
		alt=""
	/>
	{event.host.displayname}
</b>
just raided with
<b style:--user-color={event.host.color || 'red'}>{event.viewers}</b>
viewers

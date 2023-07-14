<script>
	export let event;
</script>

<!-- TODO Localize -->

<b style:--user-color={event.follower.color || 'red'}>
	<icon
		class="user-platform-icon align-middle inline-block w-[1em] h-[1em] mr-0.5"
		data-icon="service/{event.streamer.platform.toLowerCase()}"
		alt=""
	/>
	{event.follower.displayname}
</b>
just followed

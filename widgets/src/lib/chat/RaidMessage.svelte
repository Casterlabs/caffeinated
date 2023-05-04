<script>
	export let event;
</script>

<!-- TODO Localize -->

<b style:--user-color={event.host.color || 'red'}>{event.host.displayname}</b>
just raided with
<b style:--user-color={event.host.color || 'red'}>{event.viewers}</b>
viewers

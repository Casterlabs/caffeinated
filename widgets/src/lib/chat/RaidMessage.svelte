<script>
	export let event;
</script>

<!-- TODO Localize -->

<b style:color={event.host.color || 'red'}>{event.host.displayname}</b> just raided with
<b>{event.viewers}</b> viewers

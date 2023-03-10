<script>
	export let event;
</script>

<!-- TODO Localize -->

<b style:color={event.follower.color || 'red'}>{event.follower.displayname}</b> just followed

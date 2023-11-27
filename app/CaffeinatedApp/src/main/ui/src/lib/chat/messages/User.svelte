<script>
	import streamingServices from '$lib/streamingServices.mjs';

	export let user;

	function escapeHtml(unsafe) {
		return unsafe.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	}

	$: displayString =
		user && user.displayname.toLowerCase() == user.username.toLowerCase()
			? escapeHtml(user.displayname)
			: `${escapeHtml(user.displayname)} <span style="opacity: 60%; font-size: 75%;">(${escapeHtml(
					user.username
			  )})</span>`;
</script>

{#if user}
	<span class="richmessage-badges space-x-1" aria-hidden="true">
		{#each user.badges as badge}
			<img class="inline-block align-middle h-[1em] -translate-y-0.5" alt="" src={badge} />
		{/each}
	</span>

	<b
		style:--platform-color={streamingServices[user.platform]?.color}
		style:--user-color={user.color}
	>
		<icon
			class="user-platform w-[1em] h-[1em] mr-0.5 translate-y-0.5"
			data-icon="service/{user.platform.toLowerCase()}"
			style:color={streamingServices[user.platform]?.color}
		/><a href={user.link} target="_blank">{@html displayString}</a></b
	>
{/if}

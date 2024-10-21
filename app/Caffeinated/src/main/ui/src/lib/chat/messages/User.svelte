<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import streamingServices from '$lib/streamingServices.mjs';

	export let user;

	function escapeHtml(unsafe) {
		return unsafe.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	}
</script>

{#if user}
	{@const displayString =
		user.displayname.toLowerCase() == user.username.toLowerCase()
			? escapeHtml(user.displayname)
			: `${escapeHtml(user.displayname)} <span style="opacity: 60%; font-size: 75%;">(${escapeHtml(
					user.username
			  )})</span>`}

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
		/><a href="#" data-user-modal-for={user.UPID}>{@html displayString}</a><span
			class="ml-1 pronouns text-base-11 bg-base-6 border border-base-7 rounded-lg py-px px-1 text-xs"
			><LocalizedText
				key="co.casterlabs.caffeinated.app.pronouns.{user.pronouns || 'UNKNOWN'}"
			/></span
		></b
	>
{/if}

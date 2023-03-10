<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';

	let usernameInput = '';
	let isLoading = false;
	let errorMessage = null;

	async function submit() {
		if (isLoading) {
			return;
		}

		errorMessage = '';
		isLoading = true;

		try {
			const shouldGoBack = location.search != '?dontGoBack'; // Default: true

			await Caffeinated.auth.loginKick(usernameInput, shouldGoBack);
		} catch (ex) {
			isLoading = false;

			const error = JSON.parse(ex.substring(ex.indexOf('{'), ex.lastIndexOf('}') + 1));
			errorMessage = JSON.stringify(error);
			console.error(error);
		}
	}
</script>

<div class="mt-8 flex flex-col items-center">
	<h1 class="text-2xl font-medium">Sign in with Kick</h1>
	<h2 class="text-sm">We just need your username for now.</h2>
	<br />

	<div class="mt-4 w-72">
		<span
			on:keydown={(e) => {
				if (e.code == 'Enter') {
					e.preventDefault();
					submit();
				}
			}}
		>
			<SlimTextArea placeholder="Username" rows="1" resize={false} bind:value={usernameInput} />
		</span>

		<br />
		<span>
			{#if errorMessage}
				{errorMessage}
				<br />
				<br />
			{/if}
		</span>

		<button
			type="button"
			class="relative w-full mt-1.5 cursor-pointer rounded-md py-1.5 px-2 transition-[background-color] bg-base-3 border border-base-6 hover:bg-base-5 hover:border-base-8 focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7 text-center text-xs"
			on:click={submit}
		>
			{#if isLoading}
				<div class="w-4 mx-auto">
					<LoadingSpinner />
				</div>
			{:else}
				Sign In
			{/if}
		</button>
	</div>

	<!-- svelte-ignore a11y-missing-attribute -->
	<a onclick="history.back()" class="mt-4 text-md text-nqp cursor-pointer">
		<LocalizedText key="navigation.want_to_go_back" />
	</a>
</div>

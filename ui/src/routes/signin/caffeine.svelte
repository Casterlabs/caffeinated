<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import SlimButton from '$lib/ui/SlimButton.svelte';
	import SlimPassword from '$lib/ui/SlimPassword.svelte';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';

	let usernameInput = '';
	let passwordInput = '';
	let mfaInput = '';

	let isLoading = false;
	let isMfaPrompt = false;

	let errorMessage = null;

	async function submit(e) {
		if (isLoading) {
			return;
		}

		errorMessage = '';
		isLoading = true;

		try {
			const shouldGoBack = location.search != '?dontGoBack'; // Default: true

			await Caffeinated.auth.loginCaffeine(usernameInput, passwordInput, mfaInput, shouldGoBack);
		} catch (ex) {
			isLoading = false;

			if (ex.includes('MFA')) {
				isMfaPrompt = true;
			} else {
				errorMessage = JSON.stringify(ex);
				console.error(ex);
			}
		}
	}
</script>

<div class="mt-8 flex flex-col items-center">
	<h1 class="text-2xl font-medium">Sign in with Caffeine</h1>
	<br />

	<div class="mt-4 w-72">
		<span
			on:keydown={(e) => {
				if (e.code == 'Enter') {
					e.preventDefault();
					document.querySelector('input[type=password]').focus();
				}
			}}
		>
			<SlimTextArea placeholder="Username" rows="1" resize={false} bind:value={usernameInput} />
		</span>

		<span on:keydown={(e) => e.code == 'Enter' && submit()}>
			<SlimPassword placeholder="Password" bind:value={passwordInput} />

			{#if isMfaPrompt}
				<div class="mt-1.5">
					<SlimPassword placeholder="Two Factor Code" bind:value={mfaInput} autofocus={true} />
				</div>
			{/if}
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

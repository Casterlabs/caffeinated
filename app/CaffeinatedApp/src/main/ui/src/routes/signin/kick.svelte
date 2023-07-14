<script>
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Modal from '$lib/ui/Modal.svelte';
	import SlimPassword from '$lib/ui/SlimPassword.svelte';
	import SlimTextArea from '$lib/ui/SlimTextArea.svelte';

	let usernameInput = '';
	let passwordInput = '';
	let mfaInput = '';

	let isLoading = false;
	let isMfaPrompt = false;

	let errorMessage = null;

	let currentModal = null;

	async function submit(e) {
		if (isLoading) {
			return;
		}

		errorMessage = '';
		isLoading = true;

		try {
			const shouldGoBack = location.search != '?dontGoBack'; // Default: true

			await Caffeinated.auth.loginKick(usernameInput, passwordInput, mfaInput, shouldGoBack);
		} catch (ex) {
			isLoading = false;

			if (ex.includes('MFA')) {
				isMfaPrompt = true;
			} else {
				errorMessage = 'Username or password is incorrect.';
				console.error(ex);
			}
		}
	}
</script>

{#if currentModal == 'looking_for_oauth'}
	<Modal on:close={() => (currentModal = null)}>
		<LocalizedText slot="title" key="page.signin.kick.looking_for_oauth.modal.title" />
		<LocalizedText key="page.signin.kick.looking_for_oauth.modal.content" />
	</Modal>
{:else if currentModal == 'keeps_saying_incorrect'}
	<Modal on:close={() => (currentModal = null)}>
		<LocalizedText slot="title" key="page.signin.kick.keeps_saying_incorrect.modal.title" />
		<LocalizedText key="page.signin.kick.keeps_saying_incorrect.modal.content" />
	</Modal>
{/if}

<div class="mt-8 flex flex-col items-center">
	<h1 class="text-2xl font-medium">Sign in with Kick</h1>
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

	<button on:click={() => history.back()} class="mt-4 mb-2 text-md text-nqp cursor-pointer">
		<LocalizedText key="navigation.want_to_go_back" />
	</button>

	<button
		on:click={() => (currentModal = 'looking_for_oauth')}
		class="mt-1 text-xs text-nqp cursor-pointer"
	>
		<LocalizedText key="page.signin.kick.looking_for_oauth" />
	</button>

	<button
		on:click={() => (currentModal = 'keeps_saying_incorrect')}
		class="mt-1 text-xs text-nqp cursor-pointer"
	>
		<LocalizedText key="page.signin.kick.keeps_saying_incorrect" />
	</button>
</div>

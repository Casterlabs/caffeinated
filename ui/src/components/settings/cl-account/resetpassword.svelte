<script>
    import Layout from "./layout.svelte";
    import * as Account from "../../account.mjs";

    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import translate from "$lib/translate.mjs";
    import App from "$lib/app.mjs";

    export let currentScreen;

    let emailInput = "";

    let errorMessage = "";
    let isLoading = false;
    let response = null;

    async function submit() {
        if (!response) {
            isLoading = true;

            try {
                await Account.requestPasswordReset(emailInput);
                response = "casterlabs_account.request_password_reset.success";
            } catch (e) {
                console.error(e);

                if (Array.isArray(e)) {
                    const note = e[1];

                    if (note.startsWith("Invalid body value {email:")) {
                        errorMessage = "casterlabs_account.invalid_email";
                    } else {
                        errorMessage = note;
                    }
                } else {
                    errorMessage = "casterlabs_account.error";
                }
            } finally {
                isLoading = false;
            }
        }
    }
</script>

<Layout>
    {#if response}
        <br />
        <br />
        <h1 class="title has-text-centered is-6">
            <LocalizedText key={response} />
        </h1>
        <br />
        <br />
        <br />
        <br />

        <!-- svelte-ignore a11y-missing-attribute -->
        <div class="has-text-centered">
            <a on:click={() => (currentScreen = "LOGIN")}>
                <LocalizedText key="casterlabs_account.back_to_login" />
            </a>
        </div>
    {:else}
        <h1 class="title has-text-centered">
            <LocalizedText key="casterlabs_account.request_password_reset" />
        </h1>
        <br />

        <div class="field is-grouped">
            <div class="control is-expanded">
                <input
                    class="input {errorMessage.length > 0 ? 'is-danger' : ''}"
                    type="text"
                    placeholder={translate(App.get("language"), "generic.email")}
                    bind:value={emailInput}
                />

                {#if errorMessage.length > 0}
                    <p class="help is-danger">
                        <LocalizedText key={errorMessage} />
                    </p>
                {/if}
            </div>

            <div class="control">
                <button class="button is-link {isLoading ? 'is-loading' : ''}" on:click={submit}>
                    <LocalizedText key="casterlabs_account.send_email" />
                </button>
            </div>
        </div>

        <br />
        <br />

        <!-- svelte-ignore a11y-missing-attribute -->
        <div class="has-text-centered">
            <a on:click={() => (currentScreen = "LOGIN")}>
                <LocalizedText key="casterlabs_account.know_your_password" />
            </a>
        </div>
    {/if}
</Layout>

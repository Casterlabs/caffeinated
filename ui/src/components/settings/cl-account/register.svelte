<script>
    import Layout from "./layout.svelte";
    import * as Account from "../../account.mjs";

    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import translate from "$lib/translate.mjs";
    import App from "$lib/app.mjs";

    export let account;
    export let currentScreen;

    let nameInput = "";
    let emailInput = "";
    let passwordInput = "";

    let errorMessage = "";
    let isLoading = false;

    async function submit() {
        if (nameInput.length == 0 || passwordInput.length == 0) {
            return;
        }

        errorMessage = "";
        isLoading = true;

        try {
            account = await Account.register(nameInput, emailInput, passwordInput);
            currentScreen = "ME";
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
</script>

<Layout>
    <h1 class="title has-text-centered">
        <LocalizedText key="casterlabs_account.create_an_account" />
    </h1>
    <br />

    <div class="field">
        <div class="control">
            <input class="input" type="text" placeholder={translate(App.get("language"), "generic.name")} bind:value={nameInput} />
        </div>
    </div>
    <div class="field">
        <div class="control">
            <input class="input {errorMessage.length > 0 ? 'is-danger' : ''}" type="text" placeholder={translate(App.get("language"), "generic.email")} bind:value={emailInput} />
        </div>

        {#if errorMessage.length > 0}
            <p class="help is-danger">
                <LocalizedText key={errorMessage} />
            </p>
        {/if}
    </div>
    <div class="field is-grouped">
        <div class="control is-expanded">
            <input class="input" type="password" placeholder={translate(App.get("language"), "generic.password")} bind:value={passwordInput} />
        </div>
        <div class="control">
            <button class="button is-link {isLoading ? 'is-loading' : ''}" on:click={submit}>
                <LocalizedText key="casterlabs_account.register" />
            </button>
        </div>
    </div>

    <br />
    <br />

    <!-- svelte-ignore a11y-missing-attribute -->
    <div class="has-text-centered">
        <a on:click={() => (currentScreen = "LOGIN")}>
            <LocalizedText key="casterlabs_account.already_have_an_account" />
        </a>
    </div>
</Layout>

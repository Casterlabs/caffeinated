<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    let usernameInput = "";
    let passwordInput = "";
    let mfaInput = "";

    let isLoading = false;
    let isMfaPrompt = false;

    let errorMessage = null;

    async function submit(e) {
        if (!e.code || e.code == "Enter") {
            if (isLoading) {
                return;
            }

            errorMessage = "";
            isLoading = true;

            try {
                await Caffeinated.auth.loginCaffeine(usernameInput, passwordInput, mfaInput);
                // Success!
            } catch (ex) {
                if (ex.includes("MFA")) {
                    isMfaPrompt = true;
                } else {
                    const error = JSON.parse(ex.substring(ex.indexOf("{"), ex.lastIndexOf("}") + 1));

                    if (error.otp) {
                        errorMessage = "The Two Factor code is expired or incorrect.";
                    } else if (error._error) {
                        switch (error._error[0]) {
                            case "The username or password provided is incorrect": {
                                errorMessage = "The username or password provided is incorrect.";
                                break;
                            }
                        }
                    } else {
                        errorMessage = JSON.stringify(error);
                        console.error(error);
                    }
                }
            } finally {
                isLoading = false;
            }
        }
    }

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(() => {
        document.title = "";
    });
</script>

<div id="signin-container" class="has-text-centered no-select">
    <br />
    <br />
    <br />
    <h1 class="title is-4">Sign in with Caffeine</h1>
    <br />
    <div>
        <input
            id="username-input"
            class="input"
            type="text"
            placeholder="Username"
            bind:value={usernameInput}
            on:keydown={(e) => e.code == "Enter" && document.querySelector("#password-input").focus()}
        />
        <br />
        <input id="password-input" class="input" type="password" placeholder="Password" bind:value={passwordInput} on:keydown={submit} />
        <br />
        <div>
            {#if isMfaPrompt}
                <br />
                <input id="mfa-input" class="input is-success" type="text" placeholder="Two Factor Code" bind:value={mfaInput} on:keydown={submit} />
                <br />
            {/if}
        </div>
        <br />
        <span id="error-message" class="is-6 is-danger">
            {#if errorMessage}
                {errorMessage}
                <br />
            {/if}
        </span>
        <button id="signin-submit" class="button" class:is-loading={isLoading} on:click={submit}> Sign In </button>
        <br />
        <br />
        <!-- svelte-ignore a11y-missing-attribute -->
        <a onclick="history.back()" style="color: var(--theme);"> Want to go back? </a>
    </div>
</div>

<style>
    .input,
    .button {
        width: 280px;
    }
</style>

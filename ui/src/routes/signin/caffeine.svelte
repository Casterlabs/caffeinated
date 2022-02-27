<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

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
        <input id="username-input" class="input" type="text" placeholder="Username" />
        <br />
        <input id="password-input" class="input" type="password" placeholder="Password" />
        <br />
        <div class="hidden">
            <br />
            <input id="mfa-input" class="input is-success" type="text" placeholder="Two Factor Code" />
            <br />
        </div>
        <br />
        <span id="error-message" class="hidden is-6" />
        <button id="signin-submit" class="button"> Sign In </button>
        <br />
        <br />
        <!-- svelte-ignore a11y-missing-attribute -->
        <a onclick="history.back()" style="color: var(--theme);"> Want to go back? </a>
    </div>
    <script>
        {
            const signinContainer = document.querySelector("#signin-container");
            const usernameInput = signinContainer.querySelector("#username-input");
            const passwordInput = signinContainer.querySelector("#password-input");
            const mfaInput = signinContainer.querySelector("#mfa-input");
            const errorMessage = signinContainer.querySelector("#error-message");
            const signinSubmit = signinContainer.querySelector("#signin-submit");

            /* ------------ */
            /* Helpers      */
            /* ------------ */

            function signinCaffeine(username, password, mfa) {
                return new Promise((resolve, reject) => {
                    const loginPayload = {
                        account: {
                            username: username,
                            password: password
                        },
                        mfa: {
                            otp: mfa
                        }
                    };

                    fetch("https://api.caffeine.tv/v1/account/signin", {
                        method: "POST",
                        body: JSON.stringify(loginPayload),
                        headers: new Headers({
                            "Content-Type": "application/json"
                        })
                    })
                        .then((result) => result.json())
                        .then((response) => {
                            if (response.hasOwnProperty("next")) {
                                reject("CAFFEINE_MFA");
                            } else if (response.errors) {
                                reject(response.errors);
                            } else {
                                const refreshToken = response.refresh_token;

                                fetch(`https://api.casterlabs.co/v2/natsukashii/create?platform=CAFFEINE&token=${refreshToken}`)
                                    .then((nResult) => nResult.json())
                                    .then((nResponse) => {
                                        if (nResponse.data) {
                                            resolve(nResponse.data.token);
                                        } else {
                                            reject(response.errors);
                                        }
                                    });
                            }
                        })
                        .catch(reject);
                });
            }

            function clearFields() {
                usernameInput.value = "";
                passwordInput.value = "";
                mfaInput.value = "";
                mfaInput.parentElement.classList.add("hidden");
                clearError();
            }

            function clearError() {
                usernameInput.classList.remove("is-danger");
                passwordInput.classList.remove("is-danger");
                mfaInput.classList.remove("is-danger");
                errorMessage.innerHTML = "";
                errorMessage.classList.add("hidden");
            }

            function setErrorMessage(message) {
                errorMessage.innerHTML = `${message} <br /><br />`;
                errorMessage.classList.remove("hidden");
            }

            /* ------------ */
            /* UX Features  */
            /* ------------ */

            // Make it so when you hit enter on the username input
            // it'll automagically take you to the password field.
            usernameInput.addEventListener("keyup", (e) => {
                if (e.code == "Enter") {
                    passwordInput.focus();
                }
            });

            // Make the password and mfa fields trigger a signin attempt on enter.
            {
                passwordInput.addEventListener("keyup", (e) => {
                    if (e.code == "Enter") {
                        signinSubmit.click();
                    }
                });

                mfaInput.addEventListener("keyup", (e) => {
                    if (e.code == "Enter") {
                        signinSubmit.click();
                    }
                });
            }

            /* ------------ */
            /*    *Magic*   */
            /* ------------ */

            signinSubmit.addEventListener("click", async () => {
                signinSubmit.classList.add("is-loading");
                clearError();

                try {
                    const token = await signinCaffeine(usernameInput.value, passwordInput.value, mfaInput.value);

                    Bridge.emit("auth:caffeine_signin", { token: token });
                } catch (e) {
                    if (e == "CAFFEINE_MFA") {
                        clearError();
                        mfaInput.parentElement.classList.remove("hidden");
                        mfaInput.focus();
                    } else {
                        if (e.otp) {
                            mfaInput.classList.add("is-danger");
                            mfaInput.focus();
                            setErrorMessage("The Two Factor code is expired or incorrect.");
                        } else if (e._error) {
                            switch (e._error[0]) {
                                case "The username or password provided is incorrect": {
                                    usernameInput.classList.add("is-danger");
                                    passwordInput.classList.add("is-danger");
                                    passwordInput.focus();
                                    setErrorMessage("The username or password is incorrect.");
                                    break;
                                }
                            }
                        } else {
                            setErrorMessage(JSON.stringify(e));
                            console.error(e);
                        }
                    }
                } finally {
                    signinSubmit.classList.remove("is-loading");
                }
            });
        }
    </script>
</div>

<style>
    .input,
    .button {
        width: 280px;
    }
</style>

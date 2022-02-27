<script>
    import { onDestroy, onMount } from "svelte";

    import { KinokoV1 } from "../../components/kinoko.mjs";

    let state = "INSERT_CODE";
    let isLoading = false;
    let kinoko;

    let authData;

    let pairingCode = "";
    let selectedPlatform = "";
    let deviceType = "";

    function submitCode() {
        if (!isLoading && pairingCode.length == 8) {
            isLoading = true;

            kinoko.connect(`casterlabs_pairing:${pairingCode}`);
        }
    }

    function cancelSignin() {
        state = "INSERT_CODE";
        isLoading = false;
        pairingCode = "";
        deviceType = "";
        kinoko.disconnect();
    }

    function sendToken() {
        kinoko.send(`token:${authData[selectedPlatform].token}`, false);
        kinoko.disconnect();
        state = "SUCCESS";
    }

    function confirmDevice() {
        const keys = Object.keys(authData);

        selectedPlatform = keys[0];

        if (keys.length == 1) {
            sendToken();
        } else {
            state = "SELECT_ACCOUNT";
        }
    }

    onMount(async () => {
        authData = (await Bridge.query("auth")).data.koiAuth;

        kinoko = new KinokoV1();

        kinoko.on("open", () => {
            // This is the timeout checker.
            setTimeout(() => {
                if (state == "INSERT_CODE") {
                    cancelSignin();
                }
            }, 750);

            kinoko.send("what", false);
        });

        kinoko.on("orphaned", cancelSignin);

        kinoko.on("message", ({ message }) => {
            if (message.startsWith("what:")) {
                deviceType = message.substring("what:".length);
                state = "CONFIRM_DEVICE";
                isLoading = false;
            } else if (message == "success") {
                state = "DONE";
            }
        });
    });

    onDestroy(() => {
        kinoko.disconnect();
    });

    function prettifyString(str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
<div class="no-select pair-a-device has-text-centered">
    {#if state == "INSERT_CODE"}
        <h1 class="title is-5">Insert the eight digit code shown on your device</h1>

        <br />

        <div class="field has-addons has-addons-centered" style="width: 250px; margin: auto;">
            <p class="control is-fullwidth">
                <input
                    class="input centered-field"
                    type="input"
                    placeholder="12345678"
                    bind:value={pairingCode}
                    style="width: 275px;"
                    maxlength="8"
                    on:keypress={(event) => {
                        if (event.key == "Enter") {
                            submitCode();
                        } else if (!["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"].includes(event.key)) {
                            event.preventDefault();
                        }
                    }}
                    on:submit={submitCode}
                    readonly={isLoading}
                />
            </p>
            <p class="control">
                <button class="button {isLoading ? 'is-loading' : ''}" on:click={submitCode}>
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="22"
                        height="22"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="feather feather-arrow-right"
                    >
                        <line x1="5" y1="12" x2="19" y2="12" />
                        <polyline points="12 5 19 12 12 19" />
                    </svg>
                </button>
            </p>
        </div>
    {:else if state == "CONFIRM_DEVICE"}
        <h1 class="title is-5">
            Are you sure you want to pair with <i>{deviceType}</i>?
        </h1>

        <br />

        <div class="has-text-centered">
            <button class="button" on:click={cancelSignin}> No </button>
            &nbsp; &nbsp; &nbsp;
            <button class="button" on:click={confirmDevice}> Yes </button>
        </div>
    {:else if state == "SELECT_ACCOUNT"}
        <h1 class="title is-5">Which account do you want to use?</h1>

        <br />

        <div class="field has-addons has-addons-centered" style="width: 250px; margin: auto;">
            <p class="control is-fullwidth">
                <span class="select">
                    <select bind:value={selectedPlatform}>
                        {#each Object.keys(authData) as platform}
                            <option value={platform}>{prettifyString(platform)}</option>
                        {/each}
                    </select>
                </span>
            </p>
            <p class="control">
                <button class="button {isLoading ? 'is-loading' : ''}" on:click={sendToken}>
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="22"
                        height="22"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="feather feather-arrow-right"
                    >
                        <line x1="5" y1="12" x2="19" y2="12" />
                        <polyline points="12 5 19 12 12 19" />
                    </svg>
                </button>
            </p>
        </div>
    {:else if state == "SUCCESS"}
        <h1 class="title is-5">Success!</h1>
        <p>You've successfully paired your device.</p>
        <br />
        <br />
        <!-- svelte-ignore a11y-missing-attribute -->
        <a on:click={cancelSignin}> Pair another? </a>
    {/if}
</div>

<style>
    .pair-a-device {
        margin-top: 2rem;
        width: 100%;
    }

    .centered-field {
        text-align: center;
    }

    .centered-field::-webkit-input-placeholder {
        text-align: center;
    }
</style>

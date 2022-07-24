<script>
    import { onMount } from "svelte";
    import HiddenInput from "../hidden-input.svelte";

    let developerApiKey = "";
    let conductorKey = "";
    let conductorPort = "";
    let useBetaKoiPath = false;
    let authInstances = [];
    let caffeinatedClientId = "";

    onMount(async () => {
        const prefs = await Caffeinated.appPreferences;

        developerApiKey = prefs.developerApiKey;
        conductorKey = prefs.conductorKey;
        conductorPort = prefs.conductorPort;
        useBetaKoiPath = await Caffeinated.useBetaKoiPath;
        caffeinatedClientId = await Caffeinated.clientId;
        authInstances = Object.values(await Caffeinated.auth.authInstances);
    });

    function setUseBetaKoiPath() {
        Caffeinated.useBetaKoiPath = useBetaKoiPath;
        Caffeinated.UI.showToast("Restart the app for the setting to take effect.", "INFO");
    }

    function restart() {
        Bridge.emit("app:restart");
    }

    function restartWithConsole() {
        Bridge.emit("app:restart_with_console");
    }

    function prettifyString(str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
<div class="no-select developer-stuff">
    <HiddenInput label="Caffeinated Koi client ID" bind:value={caffeinatedClientId} />
    <HiddenInput label="Your API key" bind:value={developerApiKey} />
    <HiddenInput label="Widget key" bind:value={conductorKey} />
    <div class="field">
        <label class="label">Widget port</label>
        <div class="control">
            <input class="input" type="input" readonly value={conductorPort} />
        </div>
    </div>
    {#each authInstances as auth}
        <HiddenInput label={prettifyString(auth.userData.platform) + " token"} value={auth.token} />
    {/each}
    <br />
    <div class="field">
        <div class="control">
            <label class="checkbox label">
                Use Beta Koi Path
                <input type="checkbox" bind:checked={useBetaKoiPath} on:change={setUseBetaKoiPath} />
            </label>
        </div>
    </div>
    <br />
    <div class="field">
        <button class="button is-fullwidth is-danger" on:click={restart}>Try to restart app</button>
    </div>
    <div class="field">
        <button class="button is-fullwidth is-danger" on:click={restartWithConsole}>Try to restart app with console window</button>
    </div>
</div>

<style>
    .developer-stuff {
        width: 400px;
    }
</style>

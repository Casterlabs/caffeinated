<script>
    import { onMount } from "svelte";
    import HiddenInput from "../hidden-input.svelte";

    let developerApiKey = "";
    let conductorKey = "";
    let conductorPort = "";
    let useBetaKoiPath = false;

    onMount(async () => {
        const prefs = await Caffeinated.appPreferences;

        developerApiKey = prefs.developerApiKey;
        conductorKey = prefs.conductorKey;
        conductorPort = prefs.conductorPort;
        useBetaKoiPath = await Caffeinated.useBetaKoiPath;
    });

    function setUseBetaKoiPath() {
        Caffeinated.useBetaKoiPath = useBetaKoiPath;
        Caffeinated.UI.showToast("Restart the app for the setting to take effect.", "INFO");
    }

    function restart() {
        Bridge.emit("app:restart");
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
<div class="no-select developer-stuff">
    <HiddenInput label="Api Key" bind:value={developerApiKey} />
    <HiddenInput label="Conductor Key" bind:value={conductorKey} />
    <div class="field">
        <label class="label">Conductor port</label>
        <div class="control">
            <input class="input" type="input" readonly value={conductorPort} />
        </div>
    </div>
    <div class="field">
        <div class="control">
            <label class="checkbox label">
                Use Beta Koi Path
                <input type="checkbox" bind:checked={useBetaKoiPath} on:change={setUseBetaKoiPath} />
            </label>
        </div>
    </div>
    <br />
    <br />
    <br />
    <div class="field">
        <button class="button is-fullwidth is-danger" on:click={restart}>Try to restart app with console window</button>
    </div>
</div>

<style>
    .developer-stuff {
        width: 400px;
    }
</style>

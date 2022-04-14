<script>
    import { onMount } from "svelte";
    import HiddenInput from "../hidden-input.svelte";

    let developerApiKey = "";
    let conductorKey = "";
    let conductorPort = "";

    let uiPreferences = {};

    function sendUpdatedUIPreferences() {
        Caffeinated.UI.updateAppearance(uiPreferences);
    }

    onMount(async () => {
        const prefs = await Caffeinated.appPreferences;

        developerApiKey = prefs.developerApiKey;
        conductorKey = prefs.conductorKey;
        conductorPort = prefs.conductorPort;

        uiPreferences = await Caffeinated.UI.preferences;
    });
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
    <br />
    <div>
        <label class="checkbox">
            <input type="checkbox" bind:checked={uiPreferences.enableStupidlyUnsafeSettings} on:change={sendUpdatedUIPreferences} />
            Enable Stupidly Unsafe Settings
        </label>
    </div>
</div>

<style>
    .developer-stuff {
        width: 400px;
    }
</style>

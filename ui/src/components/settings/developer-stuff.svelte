<script>
    import { onMount } from "svelte";
    import HiddenInput from "../hidden-input.svelte";

    let developerApiKey = "";
    let conductorKey = "";
    let conductorPort = "";

    onMount(async () => {
        const prefs = await Caffeinated.appPreferences;

        developerApiKey = prefs.developerApiKey;
        conductorKey = prefs.conductorKey;
        conductorPort = prefs.conductorPort;
    });

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
        <button class="button is-fullwidth" on:click={restart}>Restart app with console window</button>
    </div>
</div>

<style>
    .developer-stuff {
        width: 400px;
    }
</style>

<script>
    import { onDestroy, onMount } from "svelte";

    let eventHandler;

    // let zoomValue;
    let appearanceTheme;
    let appearanceIcon;
    let appearanceCloseToTray;
    let appearanceMinimizeToTray;

    let themes = [];

    function sendUpdatedPreferences() {
        Bridge.emit("ui:appearance-update", {
            theme: appearanceTheme,
            icon: appearanceIcon,
            closeToTray: appearanceCloseToTray,
            minimizeToTray: appearanceMinimizeToTray
        });
    }

    function loadPreferences(data) {
        appearanceIcon = data.icon;
        appearanceCloseToTray = data.closeToTray;
        appearanceMinimizeToTray = data.minimizeToTray;
    }

    function onThemeUpdate(data) {
        appearanceTheme = data.id;
    }

    function onThemesUpdate(t) {
        themes = t;
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();

        eventHandler.on("themes:update", onThemesUpdate);
        onThemesUpdate((await Bridge.query("themes")).data);

        eventHandler.on("theme:update", onThemeUpdate);
        onThemeUpdate((await Bridge.query("theme")).data);

        eventHandler.on("pref-update:ui", loadPreferences);
        loadPreferences((await Bridge.query("ui:preferences")).data);
    });
</script>

<div class="no-select">
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Theme
            <br />
            <div class="select">
                <select id="appearance-theme" bind:value={appearanceTheme} on:change={sendUpdatedPreferences}>
                    {#each themes as theme}
                        <option value={theme.id}>{theme.name}</option>
                    {/each}
                    <!-- <option value="system">Match System Preference</option> -->
                </select>
            </div>
        </label>
    </div>
    <br />
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Icon
            <br />
            <div class="select">
                <select id="appearance-icon" bind:value={appearanceIcon} on:change={sendUpdatedPreferences}>
                    <option value="casterlabs">Casterlabs</option>
                    <option value="pride">Pride</option>
                    <option value="moonlabs">Moonlabs</option>
                </select>
            </div>
        </label>
    </div>
    <br />
    <div>
        <label class="checkbox">
            <input type="checkbox" bind:checked={appearanceCloseToTray} on:change={sendUpdatedPreferences} />
            Close button sends to tray
        </label>
    </div>
    <div>
        <label class="checkbox">
            <input type="checkbox" bind:checked={appearanceMinimizeToTray} on:change={sendUpdatedPreferences} />
            Minimize to tray
        </label>
    </div>
</div>

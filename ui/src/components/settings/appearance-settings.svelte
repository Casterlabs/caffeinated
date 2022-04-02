<script>
    import { onMount } from "svelte";

    // let zoomValue;
    let appearanceTheme;
    let appearanceIcon;
    let appearanceCloseToTray;
    let appearanceMikeysMode;

    let themes = [];

    function sendUpdatedPreferences() {
        Caffeinated.UI.updateAppearance({
            theme: appearanceTheme,
            icon: appearanceIcon,
            closeToTray: appearanceCloseToTray,
            mikeysMode: appearanceMikeysMode
        });
    }

    onMount(async () => {
        themes = Object.values(await Caffeinated.themeManager.themes);
        appearanceTheme = (await Caffeinated.themeManager.currentTheme).id;

        const uiPreferences = await Caffeinated.UI.preferences;

        appearanceIcon = uiPreferences.icon;
        appearanceCloseToTray = uiPreferences.closeToTray;
        appearanceMikeysMode = uiPreferences.mikeysMode;
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
                    <option value="skittles">Skittles</option>
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
    <br />
    <div>
        <span class="checkbox">
            <input type="checkbox" bind:checked={appearanceMikeysMode} on:change={sendUpdatedPreferences} />
        </span>
        <a href="https://twitter.com/Casterlabs/status/1508475284944736268" rel="external">Mikey's mode</a>
    </div>
</div>

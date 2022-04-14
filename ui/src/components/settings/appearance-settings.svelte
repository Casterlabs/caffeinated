<script>
    import TextSnippet from "$lib/TextSnippet.svelte";

    import { onMount } from "svelte";

    // let zoomValue;
    let appearanceTheme;
    let appearanceIcon;
    let appearanceCloseToTray;
    let appearanceMikeysMode;
    let appearanceEmojis;

    let isTraySupported = false;

    let themes = [];

    function sendUpdatedPreferences() {
        Caffeinated.UI.updateAppearance({
            theme: appearanceTheme,
            icon: appearanceIcon,
            closeToTray: appearanceCloseToTray,
            mikeysMode: appearanceMikeysMode,
            emojiProvider: appearanceEmojis
        });
    }

    onMount(async () => {
        themes = Object.values(await Caffeinated.themeManager.themes);
        appearanceTheme = (await Caffeinated.themeManager.currentTheme).id;

        isTraySupported = await Caffeinated.isTraySupported;

        const uiPreferences = await Caffeinated.UI.preferences;

        appearanceIcon = uiPreferences.icon;
        appearanceCloseToTray = isTraySupported ? uiPreferences.closeToTray : false;
        appearanceMikeysMode = uiPreferences.mikeysMode;
        appearanceEmojis = uiPreferences.emojiProvider;
    });
</script>

<div class="no-select">
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Theme
            <br />
            <div class="select">
                <select bind:value={appearanceTheme} on:change={sendUpdatedPreferences}>
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
                <select bind:value={appearanceIcon} on:change={sendUpdatedPreferences}>
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
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Emojis <TextSnippet>😀</TextSnippet>
            <br />
            <div class="select">
                <select bind:value={appearanceEmojis} on:change={sendUpdatedPreferences}>
                    <option value="system">System</option>
                    <option value="noto-emoji">Noto Emoji</option>
                    <option value="twemoji">Twemoji</option>
                    <option value="openmoji">OpenMoji</option>
                    <!-- <option value="sensa-emoji">Sensa Emoji</option> -->
                </select>
            </div>
        </label>
    </div>
    <br />
    <div>
        <label class="checkbox" disabled={!isTraySupported}>
            <input type="checkbox" bind:checked={appearanceCloseToTray} on:change={sendUpdatedPreferences} disabled={!isTraySupported} />
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

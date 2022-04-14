<script>
    import TextSnippet from "$lib/components/TextSnippet.svelte";

    import { onMount } from "svelte";

    let isTraySupported = false;
    let themes = [];

    let theme;
    let uiPreferences = {};

    function sendUpdatedUIPreferences() {
        Caffeinated.UI.updateAppearance({
            ...uiPreferences,
            theme: theme
        });
    }

    onMount(async () => {
        themes = Object.values(await Caffeinated.themeManager.themes);
        theme = (await Caffeinated.themeManager.currentTheme).id;

        isTraySupported = await Caffeinated.isTraySupported;
        uiPreferences = await Caffeinated.UI.preferences;
    });
</script>

<div class="no-select">
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Theme
            <br />
            <div class="select">
                <select bind:value={theme} on:change={sendUpdatedUIPreferences}>
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
                <select bind:value={uiPreferences.icon} on:change={sendUpdatedUIPreferences}>
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
                <select bind:value={uiPreferences.emojiProvider} on:change={sendUpdatedUIPreferences}>
                    <option value="system">System</option>
                    <option value="noto-emoji">Noto Emoji</option>
                    <option value="twemoji">Twemoji</option>
                    <option value="openmoji">OpenMoji</option>

                    {#if uiPreferences.enableStupidlyUnsafeSettings}
                        <option value="sensa-emoji">Sensa Emoji</option>
                    {/if}
                </select>
            </div>
        </label>
    </div>
    <br />
    <div>
        <label class="checkbox" disabled={!isTraySupported}>
            <input type="checkbox" bind:checked={uiPreferences.closeToTray} on:change={sendUpdatedUIPreferences} disabled={!isTraySupported} />
            Close button sends to tray
        </label>
    </div>
    <br />
    <div>
        <span class="checkbox">
            <input type="checkbox" bind:checked={uiPreferences.mikeysMode} on:change={sendUpdatedUIPreferences} />
        </span>
        <a href="https://twitter.com/Casterlabs/status/1508475284944736268" rel="external">Mikey's mode</a>
    </div>
</div>

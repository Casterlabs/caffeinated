<script>
    import AccountBox from "./account-settings/account-box.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;

    let systemData = null;
    let spotifyData = null;
    let pretzelData = null;
    let activePlayback = null;

    function parseBridgeData(data) {
        console.debug("[MusicServicesSettings]", data);
        activePlayback = data.activePlayback;
        systemData = data.musicServices.system;
        spotifyData = data.musicServices.spotify;
        pretzelData = data.musicServices.pretzel;
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();
        eventHandler.on("music:update", parseBridgeData);
        parseBridgeData((await Bridge.query("music")).data);
    });

    function updatePretzel(e) {
        const enabled = e.target.checked;
        Bridge.emit("music:settings-update", {
            platform: "pretzel",
            settings: {
                enabled: enabled
            }
        });
    }

    function updateSystem(e) {
        const enabled = e.target.checked;
        Bridge.emit("music:settings-update", {
            platform: "system",
            settings: {
                enabled: enabled
            }
        });
    }

    function signOutSpotify() {
        Bridge.emit("music:signout", { platform: "spotify" });
    }
</script>

<div class="no-select">
    <p>
        {#if activePlayback}
            &nbsp;Now Playing:
            <img src={activePlayback.currentTrack.albumArtUrl} class="active-playback-icon" />
            <span style="user-select: all !important;">
                {activePlayback.currentTrack.title}
            </span>
            &bull;
            <span style="user-select: all !important;">
                {activePlayback.currentTrack.artists.join(", ")}
            </span>
        {/if}
    </p>

    <br />

    <div id="accounts">
        <!-- These are in order of preference btw -->
        {#if systemData}
            <AccountBox platform="system_music" platformName="System" showSignin={false}>
                <label class="checkbox">
                    <input type="checkbox" bind:checked={systemData.settings.enabled} on:change={updateSystem} />
                    Enabled
                </label>
            </AccountBox>
        {/if}
        {#if spotifyData}
            <AccountBox
                platform="spotify"
                platformName="Spotify"
                signInLink="/signin/spotify"
                bind:accountName={spotifyData.accountName}
                bind:accountLink={spotifyData.accountLink}
                bind:isSignedIn={spotifyData.isSignedIn}
                on:signout={signOutSpotify}
            />
        {/if}
        {#if pretzelData}
            <AccountBox
                platform="pretzelrocks"
                platformName="Pretzel"
                signInLink="/signin/twitch"
                bind:accountName={pretzelData.accountName}
                bind:accountLink={pretzelData.accountLink}
                bind:isSignedIn={pretzelData.isSignedIn}
                canSignOut={false}
            >
                {#if pretzelData.isSignedIn}
                    <label class="checkbox">
                        <input type="checkbox" bind:checked={pretzelData.settings.enabled} on:change={updatePretzel} />
                        Enabled
                    </label>
                {/if}
            </AccountBox>
        {/if}
    </div>
</div>

<style>
    .active-playback-icon {
        height: 32px;
        width: auto;
        vertical-align: middle;
        border-radius: 4px;
        image-rendering: pixelated;
        margin-left: 2px;
        margin-right: 2px;
    }

    #accounts {
        margin-right: 55px;
    }
</style>

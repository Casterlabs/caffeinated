<script>
    import AccountBox from "./account-settings/account-box.svelte";
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import { onMount, onDestroy } from "svelte";

    const unregister = [];

    let systemData = null;
    let spotifyData = null;
    let pretzelData = null;
    let activePlayback = null;

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(async () => {
        unregister.push(
            Caffeinated.music.mutate("activePlayback", (data) => {
                activePlayback = data;
            })
        );

        unregister.push(
            Caffeinated.music.mutate("providers", (data) => {
                systemData = data.system;
                spotifyData = data.spotify;
                pretzelData = data.pretzel;
            })
        );
    });

    function updatePretzel(e) {
        const enabled = e.target.checked;
        Caffeinated.music.updateMusicProviderSettings("pretzel", {
            enabled: enabled
        });
    }

    function updateSystem(e) {
        const enabled = e.target.checked;
        Caffeinated.music.updateMusicProviderSettings("system", {
            enabled: enabled
        });
    }

    function signOutSpotify() {
        Caffeinated.music.signoutMusicProvider("spotify");
    }
</script>

<div class="no-select">
    <p>
        {#if activePlayback}
            &nbsp;<LocalizedText key="settings.music_services.now_playing" />
            <img src={activePlayback.currentTrack.albumArtUrl} class="active-playback-icon" alt="Current Song" />
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
            <AccountBox platform="system_music" platformName="settings.music_services.service.system" showSignin={false}>
                <label class="checkbox">
                    <input type="checkbox" bind:checked={systemData.settings.enabled} on:change={updateSystem} />
                    <LocalizedText key="generic.enabled" />
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
                        <LocalizedText key="generic.enabled" />
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

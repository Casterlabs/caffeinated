<script>
    import AccountBox from "./account-settings/account-box.svelte";

    import { onDestroy, onMount } from "svelte";

    const unregister = [];

    let accounts = {
        caffeine: {},
        twitch: {},
        trovo: {},
        glimesh: {},
        brime: {},
        youtube: {},
        dlive: {}
    };

    function parseBridgeData(koiAuth) {
        // Reset accounts object
        for (const account of Object.values(accounts)) {
            account.accountName = "";
            account.accountLink = "#";
            account.tokenId = null;
            // account.isLoading = false;
            account.isSignedIn = false;
        }

        // Parse data from bridge
        for (const account of Object.values(koiAuth)) {
            if (account.userData) {
                const platform = account.userData.platform.toLowerCase();

                accounts[platform].accountName = account.userData.displayname;
                accounts[platform].accountLink = account.userData.link;
                accounts[platform].tokenId = account.tokenId;
                account.isLoading = false;
                accounts[platform].isSignedIn = true;
            } else {
                console.warn("Miss.");
                setTimeout(async () => {
                    parseBridgeData(await Caffeinated.auth.authInstances);
                }, 50);
            }
        }

        accounts = accounts; // Update dom.
    }

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(async () => {
        unregister.push(Caffeinated.auth.mutate("authInstances", parseBridgeData));
        Caffeinated.auth.cancelSignin();
    });

    function signout(event) {
        const platform = event.detail.platform.toLowerCase();
        const tokenId = accounts[platform].tokenId;

        if (tokenId) {
            Caffeinated.auth.signout(tokenId);
        }
    }

    function signin(platform) {
        platform = platform.toLowerCase();
        accounts[platform].isLoading = true;

        Caffeinated.auth.requestOAuthSignin(`caffeinated_${platform}`, true, false);
    }
</script>

<div class="no-select">
    <div id="accounts">
        <AccountBox
            platform="caffeine"
            platformName="Caffeine"
            signInLink="/signin/caffeine?fromSettings"
            bind:accountName={accounts.caffeine.accountName}
            bind:accountLink={accounts.caffeine.accountLink}
            bind:isSignedIn={accounts.caffeine.isSignedIn}
            on:signout={signout}
        />
        <AccountBox
            platform="twitch"
            platformName="Twitch"
            signInHandler={signin}
            bind:accountName={accounts.twitch.accountName}
            bind:accountLink={accounts.twitch.accountLink}
            bind:isSignedIn={accounts.twitch.isSignedIn}
            bind:isLoading={accounts.twitch.isLoading}
            on:signout={signout}
        />
        <AccountBox
            platform="trovo"
            platformName="Trovo"
            signInHandler={signin}
            bind:accountName={accounts.trovo.accountName}
            bind:accountLink={accounts.trovo.accountLink}
            bind:isSignedIn={accounts.trovo.isSignedIn}
            bind:isLoading={accounts.trovo.isLoading}
            on:signout={signout}
        />
        <AccountBox
            platform="glimesh"
            platformName="Glimesh"
            signInHandler={signin}
            bind:accountName={accounts.glimesh.accountName}
            bind:accountLink={accounts.glimesh.accountLink}
            bind:isSignedIn={accounts.glimesh.isSignedIn}
            bind:isLoading={accounts.glimesh.isLoading}
            on:signout={signout}
        />
        <AccountBox
            platform="brime"
            platformName="Brime"
            signInHandler={signin}
            bind:accountName={accounts.brime.accountName}
            bind:accountLink={accounts.brime.accountLink}
            bind:isSignedIn={accounts.brime.isSignedIn}
            bind:isLoading={accounts.brime.isLoading}
            on:signout={signout}
        />
        <AccountBox
            platform="youtube"
            platformName="YouTube (BETA)"
            signInHandler={signin}
            bind:accountName={accounts.youtube.accountName}
            bind:accountLink={accounts.youtube.accountLink}
            bind:isSignedIn={accounts.youtube.isSignedIn}
            bind:isLoading={accounts.youtube.isLoading}
            on:signout={signout}
        />
        <AccountBox
            platform="dlive"
            platformName="DLive"
            signInHandler={signin}
            bind:accountName={accounts.dlive.accountName}
            bind:accountLink={accounts.dlive.accountLink}
            bind:isSignedIn={accounts.dlive.isSignedIn}
            bind:isLoading={accounts.dlive.isLoading}
            on:signout={signout}
        />
    </div>
</div>

<style>
    #accounts {
        margin-right: 55px;
    }
</style>

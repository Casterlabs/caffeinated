<script>
    import AccountBox from "./account-settings/account-box.svelte";

    import { onDestroy, onMount } from "svelte";

    let eventHandler;

    let accounts = {
        caffeine: {},
        twitch: {},
        trovo: {},
        glimesh: {},
        brime: {}
    };

    function parseBridgeData({ koiAuth }) {
        // Reset accounts object
        for (const account of Object.values(accounts)) {
            account.accountName = "";
            account.accountLink = "#";
            account.tokenId = null;
            // account.isLoading = false;
            account.isSignedIn = false;
        }

        // Parse data from bridge
        for (const [platform, account] of Object.entries(koiAuth)) {
            accounts[platform.toLowerCase()].accountName = account.userData.displayname;
            accounts[platform.toLowerCase()].accountLink = account.userData.link;
            accounts[platform.toLowerCase()].tokenId = account.tokenId;
            account.isLoading = false;
            accounts[platform.toLowerCase()].isSignedIn = true;
        }

        accounts = accounts; // Update dom.
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();
        eventHandler.on("auth:update", parseBridgeData);
        parseBridgeData((await Bridge.query("auth")).data);

        Bridge.emit("auth:cancel-signin");
    });

    function signout(event) {
        const platform = event.detail.platform.toLowerCase();
        const tokenId = accounts[platform].tokenId;

        if (tokenId) {
            Bridge.emit("auth:signout", { tokenId: tokenId });
        }
    }

    function signin(platform) {
        platform = platform.toLowerCase();
        accounts[platform].isLoading = true;

        Bridge.emit("auth:request-oauth-signin", { platform: `caffeinated_${platform}`, isKoi: true, goBack: false });
    }
</script>

<div class="no-select">
    <div id="accounts">
        <AccountBox
            platform="caffeine"
            platformName="Caffeine"
            signInLink="/signin/caffeine"
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
    </div>
</div>

<style>
    #accounts {
        margin-right: 55px;
    }
</style>

<script>
    import { goto } from "$app/navigation";
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    export let platform;
    export let platformName;

    export let signInLink = null;
    export let signInHandler = null;

    export let accountName = null;
    export let accountLink = null;

    export let showSignin = true;
    export let isSignedIn = false;
    export let canSignOut = true;
    export let isLoading = false;

    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function sendSignout() {
        dispatch("signout", {
            platform: platform
        });
    }

    function signin(e) {
        e.target.blur();

        if (!isLoading) {
            if (signInLink) {
                goto(`${signInLink}?homeGoBack=1`);
            } else if (signInHandler) {
                signInHandler(platform);
            }
        }
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div id="account-{platform}" class="box">
    <div class="platform-logo">
        <img src="/img/services/{platform}/icon.svg" alt="{platformName} Logo" />
    </div>

    {#if isSignedIn && accountName}
        <a href={accountLink} class="platform-name open-channel" rel="external">
            <LocalizedText key={platformName} />
        </a>

        <span class="tag streamer-name" style="user-select: all !important;"> {accountName} </span>

        {#if canSignOut}
            <a on:click={sendSignout} class="tag is-danger signout-button">
                <LocalizedText key="accounts.unlink" />
            </a>
        {/if}
    {:else}
        <span class="platform-name">
            <LocalizedText key={platformName} />
        </span>

        {#if showSignin}
            <button on:click={signin} class="button tag signin-button is-success is-text {isLoading ? 'is-loading' : ''}">
                <LocalizedText key="accounts.link" />
            </button>
        {/if}
    {/if}

    <span style="margin-left: 10px;"><slot /> </span>
</div>

<style>
    :global(#account-caffeine .platform-logo) {
        top: 22px;
        left: 13px;
        width: 23px;
    }

    .box {
        position: relative !important;
        margin-bottom: 0.9rem !important;
    }

    .platform-logo img {
        /* Lazy Method */
        filter: invert(var(--white-invert-factor));
    }

    .platform-logo {
        position: absolute;
        top: 23px;
        left: 14px;
        width: 19px;
    }

    .platform-name {
        color: inherit !important;
        margin-left: 1.4em;
    }

    .open-channel {
        text-decoration: underline;
    }

    .tag {
        display: inline-block !important;
        height: 20px;
        transform: translateY(-1px);
    }

    .signin-button {
        display: inline-block;
        position: absolute;
        top: 50%;
        right: 1.5em;
        line-height: 0.75em;
        text-decoration: none;
        transform: translateY(-50%);
        text-align: center;
    }

    .signout-button {
        display: inline-block !important;
        position: absolute;
        top: 50%;
        right: 1.5em;
        transform: translateY(-50%);
        text-align: center;
    }
</style>

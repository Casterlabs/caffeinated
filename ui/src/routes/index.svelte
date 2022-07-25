<script>
    import LoadingSpinner from "../components/loading-spinner.svelte";
    import { setPageProperties } from "./__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    let logo = "casterlabs";
    let mikeysModeEnabled = false;
    let hasBetaKoiEnabled = false;

    function disableBetaKoi() {
        Caffeinated.useBetaKoiPath = false;
        Bridge.emit("app:restart");
    }

    onMount(async () => {
        document.title = "";
        const { icon, mikeysMode } = await window.Caffeinated.UI.preferences;

        logo = icon;
        mikeysModeEnabled = mikeysMode;
        hasBetaKoiEnabled = await Caffeinated.useBetaKoiPath;

        setTimeout(Caffeinated.UI.onUILoaded, mikeysMode ? (120 + 15) * 1000 /* 2m15s */ : 150);
    });
</script>

<div class="has-text-centered">
    <div class="casterlabs-wordmark">
        {#if mikeysModeEnabled}
            <img src="/img/mikeys.png" alt="Mikey's Logo" style="margin-bottom: 50px;" />
        {:else}
            <img class="light-show" src="/img/wordmark/{logo}/black.svg" alt="Casterlabs Logo" />
            <img class="dark-show" src="/img/wordmark/{logo}/white.svg" alt="Casterlabs Logo" />
        {/if}

        <div class="loading-spinner">
            <LoadingSpinner />
        </div>

        {#if mikeysModeEnabled}
            <div id="mikeys">Enjoy your <a href="https://eatmikeys.com/" rel="external">Mikey's</a> :^)</div>
        {/if}

        {#if hasBetaKoiEnabled}
            <br />
            <!-- svelte-ignore a11y-missing-attribute -->
            <small> Having connection issues? Try <a on:click={disableBetaKoi}>disabling the Koi beta.</a></small>
        {/if}
    </div>
</div>

<style>
    .casterlabs-wordmark {
        padding-top: 35%;
        transform: translateY(-50%);
    }

    .casterlabs-wordmark img {
        width: 240px;
    }

    .loading-spinner {
        width: 50px;
        height: 50px;
        margin: auto;
    }

    #mikeys {
        position: absolute;
        left: 0;
        right: 0;
        bottom: -5em;
    }
</style>

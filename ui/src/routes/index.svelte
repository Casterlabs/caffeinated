<script>
    import LoadingSpinner from "../components/loading-spinner.svelte";
    import { setPageProperties } from "./__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    let logo = "casterlabs";

    onMount(async () => {
        document.title = "";

        const { icon } = (await Bridge.query("window")).data;

        logo = icon;

        console.debug("[App]", "Signaling window:theme-loaded");
        window.Bridge.emit("ui:theme-loaded");
    });
</script>

<div class="has-text-centered">
    <div class="casterlabs-wordmark">
        <img class="light-show" src="/img/wordmark/{logo}/black.svg" alt="Casterlabs Logo" />
        <img class="dark-show" src="/img/wordmark/{logo}/white.svg" alt="Casterlabs Logo" />

        <div class="loading-spinner">
            <LoadingSpinner />
        </div>
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
</style>

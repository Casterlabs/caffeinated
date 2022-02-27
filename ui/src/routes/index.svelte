<script>
    import LoadingSpinner from "../components/loading-spinner.svelte";
    import { setPageProperties } from "./__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    let logo = "casterlabs";
    let color = "white";

    onMount(async () => {
        document.title = "";

        const { icon } = (await Bridge.query("window")).data;
        const { isDark } = (await Bridge.query("theme")).data;

        logo = icon;

        if (isDark) {
            color = "white";
        } else {
            color = "black";
        }

        console.debug("[App]", "Signaling window:theme-loaded");
        window.Bridge.emit("ui:theme-loaded");
    });
</script>

<div class="has-text-centered">
    <div class="casterlabs-wordmark">
        <img src="/img/wordmark/{logo}/{color}.svg" alt="Casterlabs Logo" />

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

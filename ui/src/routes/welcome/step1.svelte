<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(() => {
        document.title = "";
        plausible("Fresh Install");
    });
</script>

<div class="has-text-centered">
    <br />
    <br />
    <br />
    <br />
    <h1 class="title is-4">Welcome to Casterlabs.</h1>
    <h2 class="subtitle is-6">The free stream widgets service.</h2>
    <br />
    <br />
    <br />
    <br />
    <a class="button" href="/welcome/step2"> Let's get started </a>
</div>

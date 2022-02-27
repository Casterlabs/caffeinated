<script>
    import { onMount } from "svelte";

    import { setPageProperties } from "./__layout.svelte";

    let buildInfo = {};

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: false
    });

    onMount(async () => {
        document.title = "";
        buildInfo = (await Bridge.query("build")).data;
    });
</script>

<div class="has-text-centered">
    <br />
    <h1 class="title">Welcome to Caffeinated!</h1>
    <h2 class="subtitle">(Beta {buildInfo.version}-{buildInfo.commit})</h2>
</div>

<div class="has-text-centered" style="position: absolute; bottom: 2em; width: 100%;">
    <a href="https://casterlabs.co" rel="external">casterlabs</a>
</div>

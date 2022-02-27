<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(() => {
        document.title = "";
    });
</script>

<div class="has-text-centered">
    <br />
    <br />
    <h1 class="title">test page :^)</h1>
</div>

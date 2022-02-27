<script>
    import { onMount } from "svelte";
    import LoadingSpinner from "../loading-spinner.svelte";

    export let platform;
    export let isKoi = false;

    onMount(() => {
        plausible("Sign In", { props: { platform: platform } });
        Bridge.emit("auth:request-oauth-signin", { platform: platform, isKoi: isKoi });
    });

    function cancelAuth() {
        Bridge.emit("auth:cancel-signin");
        history.back();
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div class="has-text-centered no-select">
    <br />
    <br />
    <br />
    <br />
    <br />
    <LoadingSpinner />
    <br />
    <br />
    <br />
    <a on:click={cancelAuth} style="color: var(--theme);"> Want to go back? </a>
</div>

<script>
    import { onMount } from "svelte";
    import LoadingSpinner from "../loading-spinner.svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    export let platform;
    export let isKoi = false;

    onMount(() => {
        plausible("Sign In", { props: { platform: platform } });
        Caffeinated.auth.requestOAuthSignin(platform, isKoi, false);
    });

    function cancelAuth() {
        Caffeinated.auth.cancelSignin();
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
    <a on:click={cancelAuth} style="color: var(--theme);">
        <LocalizedText key="ui.want_to_go_back" />
    </a>
</div>

<script>
    import { setPageProperties } from "../__layout.svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import TwitchButton from "../../components/platforms/signin-buttons/twitch.svelte";
    import TrovoButton from "../../components/platforms/signin-buttons/trovo.svelte";
    import CaffeineButton from "../../components/platforms/signin-buttons/caffeine.svelte";
    import GlimeshButton from "../../components/platforms/signin-buttons/glimesh.svelte";
    import BrimeButton from "../../components/platforms/signin-buttons/brime.svelte";

    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(() => {
        document.title = "";

        window.gotoStep1 = function () {
            goto("/welcome/step1");
        };

        Caffeinated.auth.cancelSignin();
    });
</script>

<div class="has-text-centered">
    <br />
    <br />
    <br />
    <h1 class="title is-4">
        <LocalizedText key="accounts.signin" />
    </h1>
    <br />
    <div class="signin-buttons">
        <TwitchButton />
        <TrovoButton />
        <CaffeineButton />
        <GlimeshButton />
        <BrimeButton />
    </div>
    <br />
    <br />
    <br />
    <div class="service-disclaimer">
        <span>
            <LocalizedText key="app.service_disclaimer" />
        </span>
    </div>
</div>

<style>
    .service-disclaimer {
        width: 370px;
        margin: auto;
    }

    .service-disclaimer :global(a) {
        color: var(--theme);
    }
</style>

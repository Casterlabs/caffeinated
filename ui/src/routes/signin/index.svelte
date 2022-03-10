<script>
    import { setPageProperties } from "../__layout.svelte";

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

        Caffeinated.app.cancelSignin();
    });
</script>

<div class="has-text-centered">
    <br />
    <br />
    <br />
    <h1 class="title is-4">Sign into your favorite platform.</h1>
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
            By signing in, you agree to our
            <a href="https://casterlabs.co/terms-of-service" class="fade-on-hover" rel="external">Terms of Service</a>
            and acknowledge that our
            <a href="https://casterlabs.co/privacy-policy" class="fade-on-hover" rel="external">Privacy Policy</a>
            applies to you.
        </span>
    </div>
</div>

<style>
    .service-disclaimer {
        width: 370px;
        margin: auto;
    }

    .service-disclaimer a {
        color: var(--theme);
    }
</style>

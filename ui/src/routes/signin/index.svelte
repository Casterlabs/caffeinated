<script>
    import { setPageProperties } from "../__layout.svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import TwitchButton from "../../components/platforms/signin-buttons/twitch.svelte";
    import TrovoButton from "../../components/platforms/signin-buttons/trovo.svelte";
    import CaffeineButton from "../../components/platforms/signin-buttons/caffeine.svelte";
    import GlimeshButton from "../../components/platforms/signin-buttons/glimesh.svelte";
    import BrimeButton from "../../components/platforms/signin-buttons/brime.svelte";
    import YouTubeButton from "../../components/platforms/signin-buttons/youtube.svelte";

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
        <YouTubeButton />
    </div>

    <div class="service-disclaimer">
        <span>
            <LocalizedText key="app.service_disclaimer" slotMapping={["tos", "privacy"]}>
                <a slot="0" href="https://casterlabs.co/terms-of-service" class="fade-on-hover" rel="external">
                    <LocalizedText key="app.terms_of_service" />
                </a>
                <a slot="1" href="https://casterlabs.com/privacy-policy" target="_blank">
                    <LocalizedText key="app.privacy_policy" />
                </a>
            </LocalizedText>
        </span>
    </div>
</div>

<style>
    .service-disclaimer {
        position: absolute;
        bottom: 20px;
        width: 100%;
    }

    .service-disclaimer span {
        display: block;
        width: 340px;
        margin: auto;
    }

    .service-disclaimer :global(a) {
        color: var(--theme);
    }
</style>

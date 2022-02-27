<script>
    import { goto } from "$app/navigation";

    function go() {
        plausible("Sign In", { props: { platform: "CAFFEINE" } });
        goto("/signin/caffeine");
    }
</script>

<div class="no-select login-button">
    <!-- svelte-ignore a11y-missing-attribute -->
    <a class="button has-text-centered" on:click={go}>
        <div class="platform-logo">
            <img src="/img/services/caffeine/icon.svg" alt="Caffeine Logo" />
        </div>
        <span> Caffeine </span>
    </a>
</div>

<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: 0.5s;
        background-color: #0000ff;
    }

    .button:hover {
        transition: 0.5s;
        background-color: #0000cc;
    }

    .button span {
        z-index: 2;
    }

    .platform-logo {
        position: absolute;
        top: 7px;
        left: 10px;
        width: 24px;
    }
</style>

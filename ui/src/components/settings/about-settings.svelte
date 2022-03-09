<script>
    import { onMount } from "svelte";
    import Credits from "../credits.mjs";

    let creditsSectionOpen = false;

    let logo = "casterlabs";
    let color = "white";
    let versionString = "";
    let year = "";

    onMount(async () => {
        logo = (await Caffeinated.UI.preferences).icon;
        year = new Date().getFullYear();

        if ((await Caffeinated.themeManager.currentTheme).appearance == "DARK") {
            color = "white";
        } else {
            color = "black";
        }

        const buildInfo = await Caffeinated.buildInfo;

        if (buildInfo.isDev) {
            versionString = "Development Build";
        } else {
            versionString = `Version ${buildInfo.version}-${buildInfo.commit} (${buildInfo.buildChannel})`;
        }
    });
</script>

<div class="about no-select box">
    <div class="columns">
        <div style="width: 248px !important; flex-grow: 0;">
            <img src="/img/wordmark/{logo}/{color}.svg" alt="Casterlabs Logo" style="width: 100%;" />
        </div>
        <div class="column" style="height: 72px; margin: auto; margin-left: 20px;">
            <p>Casterlabs-Caffeinated</p>
            <p>{versionString}</p>
        </div>
    </div>

    <hr />

    <a href="https://github.com/Casterlabs/Casterlabs-Caffeinated" rel="external" class="is-block highlight-on-hover" style="line-height: 3em;">
        Github
        <span style="float: right;">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-external-link"
            >
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                <polyline points="15 3 21 3 21 9" />
                <line x1="10" y1="14" x2="21" y2="3" />
            </svg>
        </span>
    </a>

    <hr />

    <a href="https://casterlabs.co/terms-of-service" rel="external" class="is-block highlight-on-hover" style="line-height: 3em;">
        Terms of Service
        <span style="float: right;">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-external-link"
            >
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                <polyline points="15 3 21 3 21 9" />
                <line x1="10" y1="14" x2="21" y2="3" />
            </svg>
        </span>
    </a>

    <hr />

    <a href="https://casterlabs.co/privacy-policy" rel="external" class="is-block highlight-on-hover" style="line-height: 3em;">
        Privacy Policy
        <span style="float: right;">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-external-link"
            >
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                <polyline points="15 3 21 3 21 9" />
                <line x1="10" y1="14" x2="21" y2="3" />
            </svg>
        </span>
    </a>

    <hr />

    <!-- svelte-ignore a11y-missing-attribute -->
    <a on:click={() => (creditsSectionOpen = !creditsSectionOpen)} class="is-block highlight-on-hover" style="line-height: 3em;">
        Credits
        <span style="float: right;">
            {#if creditsSectionOpen}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="1.2em"
                    height="1.2em"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-chevron-up"
                    style="transform: translate(2px, 2px);"
                >
                    <polyline points="18 15 12 9 6 15" />
                </svg>
            {:else}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="1.2em"
                    height="1.2em"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-chevron-down"
                    style="transform: translate(2px, 2px);"
                >
                    <polyline points="6 9 12 15 18 9" />
                </svg>
            {/if}
        </span>
    </a>

    {#if creditsSectionOpen}
        <hr />
        <div class="credits-section">
            {#each Credits as credit}
                <div class="credit">
                    <a href={credit.url} rel="external">
                        {credit.name}
                    </a>
                    {#if credit.license}
                        <a href={credit.licenseUrl} rel="external" style="float: right;">
                            ({credit.license})
                        </a>
                    {/if}
                </div>
            {/each}
        </div>
    {/if}

    <hr />

    <div>
        <br />
        <p>Made with ♥ by Casterlabs.</p>
        <br />
        <p>Copyright {year} Casterlabs. All rights reserved.</p>
    </div>
</div>

<style>
    .credits-section {
        padding-top: 5px;
        padding-bottom: 5px;
    }

    .credit {
        height: 1.75em;
    }

    .about {
        width: 550px;
        margin: auto;
        margin-top: 25px;
        padding-left: 0;
        padding-right: 0;
    }

    .about > a,
    .about > div {
        padding-left: 15px;
        padding-right: 15px;
    }

    hr {
        margin: 0;
    }

    :global(.app-is-light) hr {
        background-color: rgb(202, 202, 202);
    }
</style>

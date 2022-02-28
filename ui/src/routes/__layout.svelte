<script context="module">
    let elementUpdateHandler = null;

    let notifications = [];

    let pageAttributes = {
        showSideBar: false,
        allowNavigateBackwards: false
    };

    function addNotification() {
        // TODO
    }

    function setPageProperties(val) {
        pageAttributes = {
            showSideBar: false,
            allowNavigateBackwards: false,

            // Spread the `val` so we have these as defaults^
            ...val
        };

        if (elementUpdateHandler) {
            elementUpdateHandler(pageAttributes);
        }

        if (typeof window != "undefined") {
            window.currentPageAttributes = pageAttributes;
        }
    }

    export { addNotification, setPageProperties };
</script>

<script>
    import SideBar from "../components/side-bar.svelte";

    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    elementUpdateHandler = update;

    let currentPageAttributes = pageAttributes;

    function update(val) {
        currentPageAttributes = val;
    }

    onMount(async () => {
        if (!window.__common) {
            window.__common = await import("$lib/__common.mjs");
        }

        if (window.Bridge) {
            window.goto = goto;

            Bridge.on("goto", ({ path }) => goto(path));

            // Bridge.on("theme:update", updateTheme);
            // updateTheme((await Bridge.query("theme")).data);
        }
    });
</script>

<section id="notifications" class="no-select" />

<section id="body-content" class="no-select">
    {#if currentPageAttributes.showSideBar}
        <div id="side-bar">
            <SideBar />
        </div>
        <style>
            :root {
                --side-bar-width: 200px;
            }
        </style>
    {/if}

    <div class="svelte-container">
        <div id="svelte">
            <slot />
        </div>
    </div>
</section>

<style>
    :root {
        --title-bar-height: 0px;
    }

    #side-bar {
        position: absolute;
        top: var(--title-bar-height);
        bottom: 0;
        left: 0;
        width: var(--side-bar-width);
    }

    .svelte-container {
        position: absolute;
        top: var(--title-bar-height);
        bottom: 0;
        left: var(--side-bar-width);
        right: 0;
        overflow-x: hidden;
        overflow-y: auto;
    }

    /* #svelte {
        min-height: 100%;
    } */
</style>

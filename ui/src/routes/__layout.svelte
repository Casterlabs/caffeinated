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

    function updateTheme(theme) {
        let html = [];

        for (const css of theme.css) {
            if (theme.isInlineCss) {
                html.push(`<style>${css}</style>`);
            } else {
                html.push(`<link rel="stylesheet" href="${css}" />`);
            }
        }

        html = html.join("");

        const documentElement = document.documentElement;

        console.info("[__layout__] Updated theme:", theme, "\n", html);
        documentElement.classList = theme.classes;
        document.querySelector("#styles").innerHTML = html;

        if (theme.isDark) {
            documentElement.classList.add("app-is-dark");
        } else {
            documentElement.classList.add("app-is-light");
        }
    }

    onMount(async () => {
        if (typeof Bridge == "undefined") {
            updateTheme({
                isDark: true,
                css: ["/css/bulma.min.css", "/css/bulma-prefers-dark.min.css"],
                classes: "bulma-dark-mode",
                name: "Dark",
                isInlineCss: false,
                id: "co.casterlabs.dark"
            });
        } else {
            window.goto = goto;

            if (!location.pathname.startsWith("/popout")) {
                Bridge.on("goto", ({ path }) => goto(path));
            }

            Bridge.on("theme:update", updateTheme);
            updateTheme((await Bridge.query("theme")).data);
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

<script context="module">
    let pageAttributes = {
        showSideBar: false,
        allowNavigateBackwards: false
    };

    function applyPageAttributes() {
        console.log(pageAttributes);
        if (pageAttributes.showSideBar) {
            document.body.classList.add("app-show-side-bar");
        } else {
            document.body.classList.remove("app-show-side-bar");
        }
    }

    export function setPageProperties(val) {
        pageAttributes = {
            showSideBar: false,
            allowNavigateBackwards: false,

            // Spread the `val` so we have these as defaults^
            ...val
        };

        if (typeof window != "undefined") {
            applyPageAttributes();
        }
    }
</script>

<script>
    import SideBar from "../components/side-bar.svelte";

    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    import App from "$lib/app.mjs";
    import { defineExternalLocale } from "$lib/translate.mjs";

    onMount(async () => {
        if (!window.__common) {
            window.__common = await import("$lib/__common.mjs");
        }

        // Common listeners
        window.App = App;

        App.on("theme", ({ id }) => {
            __common.changeTheme(id);
        });

        // Bridge
        window.onBridgeInit = async () => {
            window.goto = goto;

            Bridge.on("goto", ({ path }) => goto(path));

            Caffeinated.UI.mutate("preferences", (preferences) => {
                const { language, emojiProvider } = preferences;

                App.mutate("language", language);
                App.mutate("emojiProvider", emojiProvider);
            });

            Caffeinated.themeManager.mutate("currentTheme", (theme) => {
                if (theme) {
                    App.mutate("theme", theme);
                }
            });

            Caffeinated.plugins.mutate("loadedPlugins", (plugins) => {
                for (const plugin of plugins) {
                    defineExternalLocale(plugin.id, plugin.lang);
                }
            });
        };

        if (typeof window.Caffeinated != "undefined") {
            window.onBridgeInit();
        }

        applyPageAttributes();
    });
</script>

<section id="notifications" class="no-select" />

<section id="body-content" class="no-select">
    <div id="side-bar">
        <SideBar />
    </div>
    <div class="svelte-container">
        <div id="svelte">
            <slot />
        </div>
    </div>
</section>

<style>
    :global(:root) {
        --title-bar-height: 0;
        --side-bar-width: 0;
    }

    :global(body.app-show-side-bar) {
        --side-bar-width: 200px;
    }

    #side-bar {
        background-color: var(--step-color);
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

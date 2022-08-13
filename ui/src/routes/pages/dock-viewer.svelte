<script>
    import { setPageProperties } from "../__layout.svelte";
    import consoleHelper from "$lib/console-helper.mjs";

    const console = consoleHelper("Dock Viewer");

    import { onMount, onDestroy } from "svelte";

    import translate from "$lib/translate.mjs";
    import App from "$lib/app.mjs";

    const unregister = [];

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    let widget = null;

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(() => {
        async function waitForLoad() {
            if (location.href.includes("?dock=")) {
                const widgets = await Caffeinated.plugins.widgets;
                const widgetId = location.href.split("?dock=")[1];

                for (const w of widgets) {
                    if (w.id == widgetId) {
                        widget = w;
                        break;
                    }
                }

                document.title = "Casterlabs-Caffeinated - " + translate(App.get("language"), widget.details.friendlyName);
            } else {
                setTimeout(waitForLoad, 100);
            }
        }
        waitForLoad();
    });
</script>

{#if widget}
    <div id="widget-demo">
        <iframe src="{widget.url}&address=127.0.0.1" title="" />
    </div>
{/if}

<style>
    #widget-demo {
        position: relative;
        width: 100%;
        height: 100vh;
    }

    #widget-demo iframe {
        position: absolute;
        width: 100%;
        height: 100%;
    }
</style>

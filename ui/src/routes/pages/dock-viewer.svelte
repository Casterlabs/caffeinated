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
    let frame = null;

    let _currentTheme = null;

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }

        window.removeEventListener("message", onPostMessage);
    });

    onMount(() => {
        window.addEventListener("message", onPostMessage);

        unregister.push(Caffeinated.themeManager.mutate("currentTheme", updateTheme));

        setTimeout(async () => {
            // Wait for the URL change to be fired.
            console.log(location.href);

            const widgets = await Caffeinated.plugins.widgets;
            const widgetId = location.href.split("?dock=")[1];

            for (const w of widgets) {
                if (w.id == widgetId) {
                    widget = w;
                    break;
                }
            }

            console.debug(widget);

            document.title = "Casterlabs-Caffeinated - " + translate(App.get("language"), widget.details.friendlyName);
        }, 100);
    });

    function onPostMessage(event) {
        if (typeof event.data == "object" && event.data.call == "init" && event.data.value == widget?.id) {
            sendFrameData(event.source);
        }
    }

    function updateTheme(d) {
        _currentTheme = d;
        if (frame) {
            sendFrameData();
        }
    }

    function sendFrameData(t = frame.contentWindow) {
        t.postMessage({ call: "theme", value: _currentTheme }, "*");
    }
</script>

{#if widget}
    <div id="widget-demo">
        <iframe bind:this={frame} src="{widget.url}&address=127.0.0.1" title="" />
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

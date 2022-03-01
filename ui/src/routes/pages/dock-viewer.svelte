<script>
    import { setPageProperties } from "../__layout.svelte";
    import consoleHelper from "$lib/console-helper.mjs";

    const console = consoleHelper("Dock Viewer");

    import { onMount, onDestroy } from "svelte";

    let eventHandler;

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    let widget = null;
    let frame = null;

    let _currentTheme = null;

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();

        Bridge.on("theme:update", updateTheme);
        updateTheme((await Bridge.query("theme")).data);

        {
            const widgets = (await Bridge.query("plugins")).data.widgets;
            const widgetId = location.href.split("?dock=")[1];

            for (const w of widgets) {
                if (w.id == widgetId) {
                    widget = w;
                    break;
                }
            }

            console.debug(widget);
        }

        document.title = `Casterlabs-Caffeinated - ${widget.details.friendlyName}`;
    });

    function updateTheme(d) {
        _currentTheme = d;
        if (frame) {
            sendFrameData();
        }
    }

    function sendFrameData() {
        frame.contentWindow.postMessage({ call: "theme", value: _currentTheme });
    }
</script>

{#if widget}
    <div id="widget-demo">
        <iframe bind:this={frame} src={widget.url} title="" on:load={sendFrameData} />
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

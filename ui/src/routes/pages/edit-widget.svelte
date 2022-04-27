<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount, onDestroy } from "svelte";

    import FormElement from "../../components/form/form-element.svelte";

    import translate from "$lib/translate.mjs";
    import App from "$lib/app.mjs";
    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import { get } from "svelte/store";

    const unregister = [];

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: true
    });

    let widget = null;
    let nameEditorTextContent;
    let nameEditor;

    function editName() {
        Caffeinated.plugins.renameWidget(widget.id, nameEditorTextContent);
    }

    function clickButton(buttonId) {
        Caffeinated.plugins.clickWidgetSettingsButton(widget.id, buttonId);
    }

    function deleteWidget() {
        Caffeinated.plugins.deleteWidget(widget.id);
        history.back();
    }

    function copyWidgetUrl() {
        Caffeinated.plugins.copyWidgetUrl(widget.id);
    }

    function fixEditableDiv(elem) {
        elem.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                elem.blur();
            }
        });
    }

    // Holy shit this is so ugly.
    // For some reason svelte won't always render the component properly.
    // Sometimes it renders the component, but the data is not updated.
    let blanking = false;

    let settingsLayout = {};
    let widgetSections = [];
    let currentWidgetSection = null;

    function switchCategory(category) {
        blanking = true;

        setTimeout(() => {
            currentWidgetSection = category;
            blanking = false;
        }, 50);
    }

    function parseBridgeData(widgets) {
        const widgetId = location.href.split("?widget=")[1];

        for (const w of widgets) {
            if (w.id == widgetId) {
                widget = w;
                break;
            }
        }

        if (!objectEquals(settingsLayout, widget.settingsLayout)) {
            blanking = true;
            console.info("[Widget Editor]", "Settings layout changed, blanking.");

            setTimeout(() => {
                blanking = false;
            }, 75);
        }

        console.log(widget);

        settingsLayout = widget.settingsLayout || {};
        nameEditorTextContent = widget.name;
        widgetSections = {};
        currentWidgetSection = currentWidgetSection || (settingsLayout.sections && settingsLayout.sections[0]?.id) || null;

        for (const section of widget.settingsLayout?.sections || []) {
            widgetSections[section.id] = section;
        }
    }

    function startEditingName() {
        nameEditor.focus();
    }

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - " + translate(App.get("language"), "widgets.editor");

        unregister.push(Caffeinated.plugins.mutate("widgets", parseBridgeData));
    });

    // https://stackoverflow.com/a/6713782
    function objectEquals(x, y) {
        if (x === y) return true;

        if (!(x instanceof Object) || !(y instanceof Object)) return false;

        if (x.constructor !== y.constructor) return false;

        for (var p in x) {
            if (!x.hasOwnProperty(p)) continue;

            if (!y.hasOwnProperty(p)) return false;

            if (x[p] === y[p]) continue;

            if (typeof x[p] !== "object") return false;

            if (!objectEquals(x[p], y[p])) return false;
        }

        for (p in y) {
            if (y.hasOwnProperty(p) && !x.hasOwnProperty(p)) return false;
        }

        return true;
    }
</script>

{#if widget}
    <div class="has-text-centered">
        <div style="margin-top: 2px; margin-bottom: 1em;">
            <div class="widget-controls">
                <div>
                    <span>
                        <div
                            contenteditable="true"
                            class="title is-5 is-inline-block cursor-edit"
                            bind:this={nameEditor}
                            bind:textContent={nameEditorTextContent}
                            on:blur={editName}
                            use:fixEditableDiv
                        />

                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="16"
                            height="16"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="white"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            class="feather feather-edit-2"
                            style="filter: invert(var(--white-invert-factor));"
                            on:click={startEditingName}
                        >
                            <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
                        </svg>
                    </span>
                    <p class="subtitle is-7" style="user-select: text;">
                        <LocalizedText key={widget.details.friendlyName} />
                    </p>
                </div>
            </div>
        </div>

        <div class="tabs">
            <ul style="justify-content: center !important;">
                {#if Object.keys(widgetSections).length > 1}
                    <!-- svelte-ignore a11y-missing-attribute -->
                    {#each Object.values(widgetSections) as widgetSection}
                        {#if widgetSection.id == currentWidgetSection}
                            <li class="is-active">
                                <a>
                                    <LocalizedText key={widgetSection.name} />
                                </a>
                            </li>
                        {:else}
                            <li>
                                <a on:click={switchCategory(widgetSection.id)}>
                                    <LocalizedText key={widgetSection.name} />
                                </a>
                            </li>
                        {/if}
                    {/each}
                    <style>
                        /* Add more spacing */
                        .widget-settings {
                            top: 140px !important;
                        }
                    </style>
                {/if}
            </ul>
        </div>

        <div class="widget-settings allow-select has-text-left">
            {#if !blanking}
                {#each (widgetSections[currentWidgetSection] || []).items as widgetSettingsOption}
                    <div style="display: flex; margin-bottom: 1em;">
                        <div class="has-text-right" style="flex: 1; margin-right: 1.5em;">
                            <span class="has-text-weight-medium">
                                <LocalizedText key={widgetSettingsOption.name} />
                            </span>
                        </div>

                        <div class="has-text-center" style="width: 200px;">
                            <FormElement {widget} {widgetSettingsOption} widgetSettingsSection={widgetSections[currentWidgetSection]} />
                        </div>

                        <div style="flex: 1;" />
                    </div>
                {/each}
            {/if}

            {#if widget.details.showDemo && widget.url}
                <div class="widget-demo-container">
                    <div class="widget-demo" style="padding-bottom: {widget.details.demoAspectRatio * 100}%;">
                        <iframe src={widget.url.replace(/&mode=.*/, "&mode=DEMO")} title={translate(App.get("language"), "widgets.editor.preview")} />
                    </div>
                </div>
            {/if}
        </div>

        <button class="button back-button" onclick="history.back();">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-arrow-left"
            >
                <line x1="19" y1="12" x2="5" y2="12" />
                <polyline points="12 19 5 12 12 5" />
            </svg>
        </button>

        <div class="widget-control-buttons is-inline-block are-small">
            {#each settingsLayout.buttons || [] as buttonLayout}
                <button on:click={() => clickButton(buttonLayout.id)} class="button" title={buttonLayout.iconTitle ? buttonLayout.iconTitle : ""}>
                    {#if buttonLayout.icon}
                        <i data-feather={buttonLayout.icon} aria-hidden="true" />
                        <script>
                            feather.replace();
                        </script>
                    {/if}
                    {#if buttonLayout.text}
                        <LocalizedText key={buttonLayout.text} />
                    {/if}
                </button>
            {/each}
            <button on:click={copyWidgetUrl} class="button">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-copy"
                >
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                </svg>
            </button>
            <button on:click={deleteWidget} class="button is-danger">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-trash-2"
                >
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                    <line x1="10" y1="11" x2="10" y2="17" />
                    <line x1="14" y1="11" x2="14" y2="17" />
                </svg>
            </button>
        </div>
    </div>
{/if}

<style>
    .widget-settings {
        position: absolute;
        top: 100px;
        bottom: 4.25em;
        left: 3.5em;
        right: 3.5em;
        overflow-x: hidden;
        overflow-y: auto;
    }

    .back-button {
        position: absolute;
        bottom: 2em;
        left: 2em;
    }

    .widget-controls {
        position: relative;
        width: fit-content;
        margin: auto;
        height: 3.5em;
        padding-top: 1em;
    }

    .widget-controls .title {
        padding-bottom: 0;
        margin-bottom: 0;
    }

    .widget-control-buttons {
        position: absolute;
        bottom: 2em;
        right: 2em;
        text-align: right;
    }

    .widget-control-buttons .button {
        margin: auto;
    }

    .widget-demo-container {
        position: relative;
        margin-top: 30px;
        width: 75%;
        max-width: 400px;
        margin: auto;
    }

    .widget-demo {
        width: 100%;
    }

    .widget-demo iframe {
        position: absolute;
        width: 100%;
        height: 100%;
    }
</style>

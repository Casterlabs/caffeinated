<script>
    import CreationDropdownCategory from "../../components/widget-manager/creation-dropdown-category.svelte";
    import { setPageProperties } from "../__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    const DEFAULT_MODULE_ICON = "grid"; // https://feathericons.com/?query=grid

    let widgetCategories = {
        alerts: [],
        labels: [],
        interaction: [],
        goals: [],
        other: []
    };

    let widgets = [];

    async function renderCreationDisplay(creatableWidgets) {
        let _widgetCategories = {
            alerts: [],
            labels: [],
            interaction: [],
            goals: [],
            other: []
        };

        // We rely on the global Modules instance
        for (const creatable of creatableWidgets) {
            if (creatable.type == "WIDGET") {
                _widgetCategories[creatable.category.toLowerCase()].push({
                    name: creatable.friendlyName,
                    create: () => {
                        Bridge.emit("plugins:create-widget", { namespace: creatable.namespace, name: `${creatable.friendlyName} (New)` });
                    }
                });
            }
        }

        // This forces svelte to rerender.
        widgetCategories = _widgetCategories;
    }

    function renderWidgetTiles(loadedWidgets) {
        widgets = Object.values(loadedWidgets);
    }

    async function render(bridgeData) {
        await renderCreationDisplay(bridgeData.creatableWidgets);
        renderWidgetTiles(bridgeData.widgets);

        feather.replace();
    }

    onMount(async () => {
        document.title = "Casterlabs-Caffeinated - Widgets";

        render((await Bridge.query("plugins")).data);
    });
</script>

<div class="has-text-centered">
    <!-- All widgets -->
    <div id="all-widgets">
        {#each widgets as widget}
            {#if widget.details.type == "WIDGET"}
                <a class="button widget-tile" href="/pages/edit-widget?widget={widget.id}" title={widget.name}>
                    <i data-feather={widget.details.icon || DEFAULT_MODULE_ICON} aria-hidden="true" />
                    <p class="widget-name">
                        {widget.name}
                    </p>
                </a>
                <script>
                    feather.replace();
                </script>
            {/if}
        {/each}
    </div>

    <div id="widget-creation-dropdown">
        <div class="dropdown is-left is-up">
            <div class="dropdown-trigger">
                <button class="button" aria-haspopup="true" aria-controls="widget-creation-dropdown-content">
                    <span class="icon is-small">
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
                            class="feather feather-plus"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg
                        >
                    </span>
                </button>
            </div>
            <div class="dropdown-menu" id="widget-creation-dropdown-content" role="menu">
                <div class="dropdown-content">
                    {#if widgetCategories.alerts.length > 0}
                        <!-- "Alerts" Dropdown -->
                        <div class="dropdown-item">
                            <CreationDropdownCategory name="Alerts" icon="bell">
                                {#each widgetCategories.alerts as item}
                                    <div class="dropdown-item">
                                        <button class="button ghost-button" on:click={item.create} style="width: 11rem;">
                                            {item.name}
                                        </button>
                                    </div>
                                {/each}
                            </CreationDropdownCategory>
                        </div>
                        <hr class="dropdown-divider" />
                    {/if}

                    {#if widgetCategories.labels.length > 0}
                        <!-- "Labels" Dropdown -->
                        <div class="dropdown-item">
                            <CreationDropdownCategory name="Labels" icon="type">
                                {#each widgetCategories.labels as item}
                                    <div class="dropdown-item">
                                        <button class="button ghost-button" on:click={item.create} style="width: 11rem;">
                                            {item.name}
                                        </button>
                                    </div>
                                {/each}
                            </CreationDropdownCategory>
                        </div>
                        <hr class="dropdown-divider" />
                    {/if}

                    {#if widgetCategories.interaction.length > 0}
                        <!-- "Interaction" Dropdown -->
                        <div class="dropdown-item">
                            <CreationDropdownCategory name="Interaction" icon="message-circle">
                                {#each widgetCategories.interaction as item}
                                    <div class="dropdown-item">
                                        <button class="button ghost-button" on:click={item.create} style="width: 11rem;">
                                            {item.name}
                                        </button>
                                    </div>
                                {/each}
                            </CreationDropdownCategory>
                        </div>
                        <hr class="dropdown-divider" />
                    {/if}

                    {#if widgetCategories.goals.length > 0}
                        <!-- "Goals" Dropdown -->
                        <div class="dropdown-item">
                            <CreationDropdownCategory name="Goals" icon="bar-chart">
                                {#each widgetCategories.goals as item}
                                    <div class="dropdown-item">
                                        <button class="button ghost-button" on:click={item.create} style="width: 11rem;">
                                            {item.name}
                                        </button>
                                    </div>
                                {/each}
                            </CreationDropdownCategory>
                        </div>
                        <hr class="dropdown-divider" />
                    {/if}

                    {#if widgetCategories.other.length > 0}
                        <!-- "Other" Dropdown -->
                        <div class="dropdown-item">
                            <CreationDropdownCategory name="Other" icon="droplet">
                                {#each widgetCategories.other as item}
                                    <div class="dropdown-item">
                                        <button class="button ghost-button" on:click={item.create} style="width: 11rem;">
                                            {item.name}
                                        </button>
                                    </div>
                                {/each}
                            </CreationDropdownCategory>
                        </div>
                        <script>
                            feather.replace();
                        </script>
                    {/if}
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    #widget-creation-dropdown {
        position: fixed;
        bottom: 0.75rem;
        left: calc(var(--side-bar-width) + 0.75rem);
    }

    #widget-creation-dropdown > .dropdown > .dropdown-trigger {
        text-align: left;
        width: 3.5rem;
    }

    #widget-creation-dropdown-content > .dropdown-content {
        padding-top: 2px;
        padding-bottom: 2px;
    }

    #widget-creation-dropdown-content > .dropdown-content > .dropdown-item {
        padding: 0;
    }

    #widget-creation-dropdown > .dropdown:hover > .dropdown-menu {
        display: block;
    }

    .dropdown-divider {
        margin: 2px;
    }

    .ghost-button {
        padding: 0;
        padding-left: 0.5em;
        width: 11rem;
        justify-content: left;
        border: none;
    }

    #all-widgets {
        padding: 1em;
    }

    .widget-tile {
        display: inline-block;
        width: 150px;
        height: 150px;
        margin: 0.5em;
    }

    :global(.widget-tile svg) {
        margin-top: 0.75em;
        margin-bottom: 0.75em;
        width: 4em;
        height: 4em;
    }

    .widget-tile .widget-name {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 0.85em;
        line-height: 2em;
    }
</style>

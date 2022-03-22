<script>
    import CreationDropdownCategory from "../../components/widget-manager/creation-dropdown-category.svelte";
    import { setPageProperties } from "../__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    const DEFAULT_MODULE_ICON = "grid"; // https://feathericons.com/?query=grid

    let widgets = [];

    function copyWidgetUrl(id) {
        Caffeinated.plugins.copyWidgetUrl(id);
    }

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - Docks";

        widgets = await Caffeinated.plugins.widgets;
        feather.replace();
    });
</script>

<div class="has-text-centered">
    <!-- All widgets -->
    <div id="all-widgets">
        {#each widgets as widget}
            {#if widget.details.type == "DOCK"}
                <!-- svelte-ignore a11y-missing-attribute -->
                <a class="button widget-tile" on:click={() => copyWidgetUrl(widget.id)} title={widget.details.friendlyName}>
                    <i data-feather={widget.details.icon || DEFAULT_MODULE_ICON} aria-hidden="true" />
                    <p class="widget-name">
                        {widget.details.friendlyName}
                    </p>
                </a>
                <script>
                    feather.replace();
                </script>
            {/if}
        {/each}
    </div>
</div>

<style>
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

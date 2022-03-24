<script>
    import { onMount, onDestroy } from "svelte";

    const unregister = [];

    let docks = [];

    function render(widgets) {
        docks = widgets
            .filter((w) => w.details.type == "DOCK" || w.details.type == "APPLET")
            // .
            // We want to hide these because they're already available.
            .filter((w) => !["co.casterlabs.dock.stream_viewers.dock", "co.casterlabs.dock.stream_chat.dock"].includes(w.id));
    }

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(() => {
        unregister.push(Caffeinated.plugins.mutate("widgets", render));
    });
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div class="sidebar-container has-text-left">
    <!-- Your Stream -->
    <div class="sidebar-section">
        <a class="sidebar-category-button" href="/home"> Home </a>
    </div>

    <!-- Your Stream -->
    <div class="sidebar-section">
        <h1 class="title">Your Stream</h1>

        <a class="sidebar-category-button" href="/pages/stream-chat"> Chat </a>

        <a class="sidebar-category-button" href="/pages/chatbot-manager"> Chat Bot </a>

        {#each docks as dock}
            <a class="sidebar-category-button" href="/pages/dock-viewer?dock={dock.id}"> {dock.details.friendlyName} </a>
        {/each}
    </div>

    <!-- Channel -->
    <div class="sidebar-section hidden">
        <h1 class="title">Channel</h1>

        <a class="sidebar-category-button hidden"> Analytics </a>
        <a class="sidebar-category-button"> Custom Emotes </a>
        <a class="sidebar-category-button"> Shako </a>
    </div>

    <!-- Other -->
    <div class="sidebar-section">
        <h1 class="title">Management</h1>

        <a class="sidebar-category-button" href="/pages/widget-manager"> Widgets </a>
        <a class="sidebar-category-button" href="/pages/dock-manager"> Docks </a>
        <a class="sidebar-category-button" href="/settings"> Settings </a>
    </div>
</div>

<style>
    .sidebar-container {
        width: 100%;
        height: 100%;
        padding-top: 15px;
        overflow-y: auto;
    }

    .sidebar-section {
        padding-left: 10px;
        padding-right: 10px;
    }

    .sidebar-section > .title {
        margin-bottom: 0 !important;
    }

    .sidebar-section:not(:first-child) {
        margin-top: 15px;
    }

    .sidebar-section > .title,
    .sidebar-category-button {
        font-size: 1.15em !important;
    }

    .sidebar-category-button {
        color: unset !important;
        width: 100%;
        border-radius: 4px;
        display: block;
    }

    :not(.sidebar-section) > .sidebar-category-button {
        padding-left: 10px;
        padding-right: 10px;
    }

    .sidebar-category-button:hover {
        background-color: rgba(100, 100, 100, 0.05);
    }

    .sidebar-category-button:global(.is-selected) {
        background-color: rgba(100, 100, 100, 0.2);
    }
</style>

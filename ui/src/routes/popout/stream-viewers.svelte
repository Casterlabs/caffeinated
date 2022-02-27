<script>
    import { setPageProperties } from "../__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: true
    });

    let platforms = {};
    let viewersList = [];

    function onViewersList(e) {
        console.log(e);
        platforms[e.streamer.platform] = e.viewers;

        updateViewersList();
    }

    function onAuthUpdate({ koiAuth }) {
        const signedInPlatforms = Object.keys(koiAuth);

        for (const platform of Object.keys(platforms)) {
            if (!signedInPlatforms.includes(platform)) {
                delete platforms[platform];
            }
        }

        updateViewersList();
    }

    function updateViewersList() {
        let list = [];

        for (const viewers of Object.values(platforms)) {
            list.push(...viewers);
        }

        viewersList = list;
    }

    /* ---------------- */
    /* Life Cycle   */
    /* ---------------- */

    function mount() {
        if (!window.Koi) {
            setTimeout(mount, 500);
            return;
        }

        onAuthUpdate({ koiAuth: Koi.userStates });

        Widget.on("auth:update", onAuthUpdate);
        Widget.on("__eval", eval);

        Koi.on("viewer_list", onViewersList);

        for (const [platform, viewers] of Object.entries(Koi.viewers)) {
            onViewersList({
                streamer: {
                    platform: platform
                },
                viewers: viewers,
                event_type: "VIEWER_LIST"
            });
        }
    }

    onMount(async () => {
        document.title = "Casterlabs-Caffeinated - Stream Viewers";
        mount();
    });
</script>

<div class="viewers-list">
    <span id="total-count">
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="feather feather-eye"
            style="transform: translateY(1.5px);"
        >
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
            <circle cx="12" cy="12" r="3" />
        </svg>
        {viewersList.length}
    </span>

    <ul>
        {#each viewersList as viewer}
            <li>
                {viewer.displayname}
            </li>
        {/each}
    </ul>
</div>

<style>
    .viewers-list {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        padding: 5px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    #total-count {
        position: absolute;
        top: 5px;
        right: 9px;
        text-align: right;
    }
</style>

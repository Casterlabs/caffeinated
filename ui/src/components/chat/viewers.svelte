<script>
    let platforms = {};
    let viewersList = [];

    export function onViewersList(e) {
        console.log(e);
        platforms[e.streamer.platform] = e.viewers;

        updateViewersList();
    }

    export function onAuthUpdate(signedInPlatforms) {
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
</script>

<div class="container box">
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
    .container {
        height: 100%;
        margin: 5px;
        position: relative;
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

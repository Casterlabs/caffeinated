<script>
    import { onMount, onDestroy } from "svelte";

    const unregister = [];
    let fileWatcherId;

    let currentlyLoadedContexts = [];
    let filesInPluginsDir = [];

    let targetFileToLoad = "";

    function loadFile() {
        if (targetFileToLoad != "") {
            Caffeinated.plugins.load(targetFileToLoad);
            targetFileToLoad = "";
        }
    }

    function unload(id) {
        Caffeinated.plugins.unload(id);
    }

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }

        clearInterval(fileWatcherId);
    });

    onMount(async () => {
        Caffeinated.plugins.mutate("contexts", (v) => {
            currentlyLoadedContexts = v.filter((c) => c.pluginType == "PLUGIN");
        });

        fileWatcherId = setInterval(async () => {
            filesInPluginsDir = await Caffeinated.plugins.listFiles();
        }, 1000);
    });
</script>

<div class="plugins no-select">
    {#if currentlyLoadedContexts.length > 0}
        <h1 class="title is-6">Loaded Plugins:</h1>
        <ul>
            {#each currentlyLoadedContexts as context}
                <li class="box">
                    {context.file}
                    <!-- svelte-ignore a11y-missing-attribute -->
                    <a on:click={() => unload(context.id)} style="color: inherit; float: right;">
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
                            class="feather feather-x"
                        >
                            <line x1="18" y1="6" x2="6" y2="18" />
                            <line x1="6" y1="6" x2="18" y2="18" />
                        </svg>
                    </a>
                </li>
            {/each}
        </ul>
        <br />
        <br />
        <br />
    {/if}
    <div class="field has-addons">
        <div class="control is-expanded">
            <div class="select is-fullwidth">
                <select bind:value={targetFileToLoad}>
                    <option value="" disabled selected>Select a file to load</option>
                    {#each filesInPluginsDir as file}
                        <option>{file}</option>
                    {/each}
                </select>
            </div>
        </div>
        <div class="control">
            <button class="button" on:click={() => window.Caffeinated.plugins.openPluginsDir()}> Open Plugins Folder </button>
        </div>
        <div class="control">
            <button class="button" on:click={loadFile} style="width: 100px;"> Load </button>
        </div>
    </div>
</div>

<style>
    .plugins {
        margin-top: 20px;
        margin-left: 5px;
        margin-right: 20px;
    }

    li.box {
        padding: 10px;
    }

    .title {
        margin-bottom: 20px !important;
    }
</style>

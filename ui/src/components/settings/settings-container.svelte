<script>
    import { onMount } from "svelte";

    export let categories;
    export let updateTitle = true;

    let currentCategory = {};

    function switchCategory(category) {
        if (currentCategory !== category) {
            currentCategory = category;

            if (updateTitle) {
                document.title = `Casterlabs-Caffeinated - Settings - ${currentCategory.name}`;
            }
        }
    }

    onMount(() => {
        // Select the first category.
        for (const category of categories) {
            if (category.type == "category") {
                switchCategory(category);
                break;
            }
        }
    });
</script>

<div class="settings-container">
    <div class="settings-navigate side-bar has-text-left">
        {#each categories as category}
            {#if category.type == "section"}
                <h1 class="settings-section title is-6">
                    {category.name}
                </h1>
            {:else if category.type == "category"}
                <!-- svelte-ignore a11y-missing-attribute -->
                <a
                    class="
                        settings-category-button 
                        is-6 
                        {currentCategory == category ? 'is-selected' : ''}
                    "
                    on:click={() => switchCategory(category)}
                >
                    <span>
                        {category.name}
                    </span>
                </a>
            {/if}
        {/each}
    </div>

    <div class="settings-content has-text-left">
        <svelte:component this={currentCategory.component} />
    </div>
</div>

<style>
    .settings-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        overflow: hidden;
    }

    .settings-navigate {
        position: absolute;
        top: 0;
        left: 0;
        width: 175px;
        height: 100%;
        padding-left: 10px;
        padding-right: 10px;
        overflow-y: auto;
    }

    .settings-content {
        position: absolute;
        top: 0;
        left: 175px;
        right: 0;
        height: 100%;
        margin-left: 20px;
        overflow-y: auto;
    }

    .settings-section {
        margin-left: 10px;
        margin-bottom: 3px !important;
    }

    .settings-section:not(:first-child) {
        margin-top: 15px;
    }

    .settings-category-button {
        color: unset !important;
        width: 100%;
        margin-right: 10px;
        border-radius: 4px;
        display: block;
    }

    .settings-category-button span {
        margin-left: 10px;
    }

    .settings-category-button:hover {
        background-color: rgba(100, 100, 100, 0.05);
    }

    .settings-category-button:global(.is-selected) {
        background-color: rgba(100, 100, 100, 0.2);
    }
</style>

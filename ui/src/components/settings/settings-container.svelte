<script>
    import { onMount, onDestroy } from "svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import translate from "$lib/translate.mjs";

    export let categories;
    export let updateTitle = true;

    const unregister = [];
    let unsafeEnabled = false;

    let currentCategory = {};

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(() => {
        // Select the first category.
        for (const category of categories) {
            if (category.type == "category") {
                switchCategory(category);
                break;
            }
        }

        unregister.push(
            Caffeinated.UI.mutate("preferences", ({ enableStupidlyUnsafeSettings }) => {
                unsafeEnabled = enableStupidlyUnsafeSettings;
            })
        );
    });

    async function switchCategory(category) {
        if (currentCategory !== category) {
            currentCategory = category;

            if (updateTitle) {
                const { language } = await Caffeinated.UI.preferences;

                const settingsLang = translate(language, "settings");
                const categoryLang = translate(language, currentCategory.name);

                document.title = `Casterlabs-Caffeinated - ${settingsLang} - ${categoryLang}`;
            }
        }
    }
</script>

<div class="settings-container columns">
    <div class="settings-navigate side-bar has-text-left column is-narrow">
        {#each categories as category}
            {#if !category.unsafe || unsafeEnabled}
                {#if category.type == "section"}
                    <h1 class="settings-section title is-6">
                        <LocalizedText key={category.name} />
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
                            <LocalizedText key={category.name} />
                        </span>
                    </a>
                {/if}
            {/if}
        {/each}
    </div>

    <div class="settings-content has-text-left column">
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
        margin: 2px;
    }

    .settings-navigate {
        display: inline-block;
        width: fit-content;
        height: 100%;
        padding-left: 10px;
        padding-right: 10px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    .settings-content {
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

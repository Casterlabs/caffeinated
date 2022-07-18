<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import ApiSearchInput from "../api-search-input.svelte";

    export let selectedAccount = null;
    export let features;

    export let currentInputData;

    async function categorySearch(query) {
        const response = await //
        (await fetch(`https://api.casterlabs.co/v2/koi/stream/${selectedAccount.userData.platform}/categories/search?q=${encodeURIComponent(query)}`)) //
            .json();

        const result = [];

        if (response.data) {
            for (const item of Object.values(response.data.result)) {
                result.push(item);
            }
        }

        return result;
    }

    function markModified() {
        currentInputData.hasBeenModified = true;
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
{#if features[selectedAccount.userData.platform].includes("CATEGORY")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.category" />
        </label>
        <div class="control">
            <ApiSearchInput on:change={markModified} bind:value={currentInputData.category} defaultValue={selectedAccount.streamData?.category || ""} lookup={categorySearch} />
        </div>
    </div>
{/if}

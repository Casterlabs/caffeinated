<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import SearchInput from "../search-input.svelte";

    export let selectedAccount = null;
    export let features;
    export let languages;

    export let currentInputData;

    function markModified() {
        currentInputData.hasBeenModified = true;
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->

{#if features[selectedAccount.userData.platform].includes("LANGUAGE")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.language" />
        </label>
        <div class="control">
            <SearchInput
                bind:value={currentInputData.language}
                on:change={markModified}
                defaultValue={languages[selectedAccount.streamData?.language || "OTHER"]}
                entries={Object.values(languages)}
            />
        </div>
    </div>
{/if}

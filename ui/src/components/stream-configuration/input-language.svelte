<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import SearchInput from "../search-input.svelte";

    export let selectedAccount = null;
    export let features;
    export let languages;

    export let currentInputData;
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->

{#if features[selectedAccount.userData.platform].includes("LANGUAGE")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.language" />
        </label>
        <div class="control">
            <SearchInput bind:value={currentInputData.language} defaultValue={languages[selectedAccount.streamData?.language || "OTHER"]} entries={Object.values(languages)} />
        </div>
    </div>
{/if}

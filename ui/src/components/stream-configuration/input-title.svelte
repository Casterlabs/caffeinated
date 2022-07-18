<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    export let selectedAccount = null;
    export let features;

    export let currentInputData;

    function markModified() {
        currentInputData.hasBeenModified = true;
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
{#if features[selectedAccount.userData.platform].includes("TITLE")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.title" />
        </label>
        <div class="control">
            <input class="input" type="text" placeholder={selectedAccount.streamData.title || ""} bind:value={currentInputData.title} on:change={markModified} />
        </div>
    </div>
{/if}

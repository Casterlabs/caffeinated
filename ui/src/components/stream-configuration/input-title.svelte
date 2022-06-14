<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    export let selectedAccount = null;
    export let features;

    export let currentInputData;
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
{#if features[selectedAccount.userData.platform].includes("TITLE")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.title" />
        </label>
        <div class="control">
            <input class="input" type="text" placeholder={selectedAccount.streamData.title} bind:value={currentInputData.title} />
        </div>
    </div>
{/if}

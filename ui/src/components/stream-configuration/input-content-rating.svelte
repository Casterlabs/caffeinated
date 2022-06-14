<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";

    export let selectedAccount = null;
    export let features;
    export let contentRatings;

    export let currentInputData;
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->

{#if features[selectedAccount.userData.platform].includes("CONTENT_RATING")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.content_rating" />
        </label>
        <div class="control">
            <div class="select is-fullwidth">
                <select bind:value={currentInputData.contentRating}>
                    {#each contentRatings[selectedAccount.userData.platform] as rating}
                        <option value={rating}>
                            <LocalizedText key="stream.content_rating.{rating}" />
                        </option>
                    {/each}
                </select>
            </div>
        </div>
    </div>
{/if}

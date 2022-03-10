<script>
    import SearchInput from "./search-input.svelte";

    export let accounts = [];
    let selectedAccount = null;

    let hasInitiaized = false;

    let titleInput = "";
    let categoryInput = "";
    let languageInput = "";
    let tagsInput = [];
    let contentRatingInput = null;
    let thumbnailContent = null;

    export function render(newAccounts) {
        if (!selectedAccount) {
            select(newAccounts[0]);
        }

        if (accounts.length > 0 && !hasInitiaized) {
            hasInitiaized = true;

            // Figure out defaults.
            for (const account of accounts) {
                if (account.streamData.title && titleInput.length == 0) {
                    titleInput = account.streamData.title;
                }

                if (account.streamData.category && categoryInput.length == 0) {
                    categoryInput = account.streamData.category;
                }

                if (account.streamData.language && languageInput.length == 0) {
                    // Languages are shared.
                    languageInput = account.languages[account.streamData.language];
                }

                if (account.streamData.tags && tagsInput.length == 0) {
                    tagsInput = account.streamData.tags;
                }

                if (account.streamData.content_rating && contentRatingInput == null) {
                    contentRatingInput = account.streamData.content_rating;
                }

                if (account.streamData.thumbnail_url && thumbnailContent == null) {
                    thumbnailContent = account.streamData.thumbnail_url;
                }
            }
        }
    }

    function select(account) {
        selectedAccount = account;
    }

    function submit() {
        let languageEnum = null;

        for (const [language, lang] of Object.entries(selectedAccount.languages)) {
            if (lang == languageInput) {
                languageEnum = language;
                break;
            }
        }

        const data = {
            title: titleInput,
            category: categoryInput,
            language: languageEnum,
            tags: tagsInput,
            content_rating: contentRatingInput,
            thumbnail: thumbnailContent
        };

        Caffeinated.auth.updateStream(selectedAccount.userData.UPID, data);
    }

    render(accounts); // Select a default.
</script>

{#if accounts.length > 1}
    <nav class="pagination is-centered" aria-label="pagination">
        <!-- svelte-ignore a11y-missing-attribute -->
        <!-- svelte-ignore a11y-missing-content -->
        <ul class="pagination-list">
            {#each accounts as account}
                <li>
                    <a on:click={() => select(account)} class="pagination-link {selectedAccount == account ? 'is-current' : ''}" aria-label={account.userData.platform}>
                        <img class="nav-icon" src="/img/services/{account.userData.platform.toLowerCase()}/icon.svg" alt={account.userData.platform} />
                    </a>
                </li>
            {/each}
        </ul>
    </nav>
{/if}

<!-- svelte-ignore a11y-missing-attribute -->
<!-- svelte-ignore a11y-label-has-associated-control -->
{#if selectedAccount}
    <div style="width: 400px; margin: auto;">
        {#if selectedAccount.streamConfigurationFeatures.includes("TITLE")}
            <div class="field">
                <label class="label">Stream Title</label>
                <div class="control">
                    <input class="input" type="text" placeholder={selectedAccount.streamData.title} bind:value={titleInput} />
                </div>
            </div>
        {/if}

        {#if selectedAccount.streamConfigurationFeatures.includes("CONTENT_RATING")}
            <div class="field">
                <label class="label">Content Rating</label>
                <div class="control">
                    <div class="select is-fullwidth">
                        <select bind:value={contentRatingInput}>
                            {#each Object.entries(selectedAccount.contentRatings) as [rating, lang]}
                                <option value={rating}>{lang}</option>
                            {/each}
                        </select>
                    </div>
                </div>
            </div>
        {/if}

        {#if selectedAccount.streamConfigurationFeatures.includes("CATEGORY")}
            <div class="field">
                <label class="label">Category</label>
                <div class="control">
                    <SearchInput bind:value={categoryInput} defaultValue={selectedAccount.streamData.category} entries={Object.values(selectedAccount.streamCategories)} />
                </div>
            </div>
        {/if}

        {#if selectedAccount.streamConfigurationFeatures.includes("LANGUAGE")}
            <div class="field">
                <label class="label">Language</label>
                <div class="control">
                    <SearchInput
                        bind:value={languageInput}
                        defaultValue={selectedAccount.languages[selectedAccount.streamData.language]}
                        entries={Object.values(selectedAccount.languages)}
                    />
                </div>
            </div>
        {/if}

        <!-- TODO TAGS & THUMBNAIL -->

        <a on:click={submit} class="button is-fullwidth" style="margin-top: 25px;">
            <img class="nav-icon" src="/img/services/{selectedAccount.userData.platform.toLowerCase()}/icon.svg" alt={selectedAccount.userData.platform} />
            &nbsp;&nbsp;Update Stream Info
        </a>
    </div>
{/if}

<style>
    .nav-icon {
        width: 24px;
        height: 24px;
        filter: invert(var(--white-invert-factor));
    }
</style>

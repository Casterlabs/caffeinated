<script>
    import { onMount } from "svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import InputTitle from "./input-title.svelte";
    import InputCategory from "./input-category.svelte";
    import InputLanguage from "./input-language.svelte";
    import InputContentRating from "./input-content-rating.svelte";

    const PLATFORMS = ["CAFFEINE", "TWITCH", "TROVO"]; // The only ones supported.

    export let accounts = [];

    // Current
    let selectedAccount = null;
    let currentInputData = null;
    let currentPlatform = null;

    // Inputs
    let inputs = {};

    // Features and static data, populated in onMount.
    let languages = {};
    let contentRatings = {};
    let features = {};

    // Populate
    for (let platform of PLATFORMS) {
        inputs[platform] = {
            title: null,
            category: null,
            language: null,
            contentRating: null,
            tags: null,
            thumbnailFile: null,
            thumbnailUrl: null
        };
        features[platform] = [];
        contentRatings[platform] = [];
    }

    export function render(newAccounts) {
        if (newAccounts) accounts = newAccounts;

        if (!selectedAccount && accounts[0]) {
            select(accounts[0]);
        }

        for (let account of accounts) {
            const inputData = inputs[account.userData.platform];

            if (!inputData.title) {
                inputData.title = account.streamData.title;
            }

            if (!inputData.category) {
                inputData.category = account.streamData.category;
            }

            if (!inputData.language && languages) {
                inputData.language = languages[account.streamData.language];
            }

            if (!inputData.contentRating) {
                inputData.contentRating = account.streamData.content_rating;
            }

            if (!inputData.tags) {
                inputData.tags = account.streamData.tags;
            }

            if (!inputData.thumbnailUrl) {
                inputData.thumbnailUrl = account.streamData.thumbnail_url;
            }
        }
    }

    render(accounts); // Select a default.

    function select(account) {
        selectedAccount = account;
        currentPlatform = account.userData.platform;
        currentInputData = inputs[currentPlatform];
    }

    function submit() {
        // let languageEnum = null;
        // for (const [language, lang] of Object.entries(selectedAccount.languages)) {
        //     if (lang == languageInput) {
        //         languageEnum = language;
        //         break;
        //     }
        // }
        // // TODO lookup the input for the category and get the ID.
        // const streamUpdatePayload = {
        //     title: titleInput,
        //     category: categoryInput,
        //     language: languageEnum,
        //     tags: tagsInput,
        //     content_rating: contentRatingInput
        // };
        // fetch("https://api.casterlabs.co/v2/koi/stream/update", {
        //     method: "POST",
        //     headers: {
        //         "Content-Type": "application/json",
        //         Authorization: "Koi " + selectedAccount.token
        //     },
        //     body: JSON.stringify(streamUpdatePayload)
        // });
        // if (thumbnailFile) {
        //     fetch("https://api.casterlabs.co/v2/koi/stream/thumbnail/update", {
        //         method: "POST",
        //         headers: {
        //             "Content-Type": "application/json",
        //             Authorization: "Koi " + selectedAccount.token
        //         },
        //         body: thumbnailFile
        //     });
        // }
    }

    onMount(async () => {
        languages = await Caffeinated.auth.getLanguages();

        for (const platform of Object.keys(features)) {
            (async () => {
                const featuresData = await (await fetch(`https://api.casterlabs.co/v2/koi/stream/${platform}/features`)).json();

                if (featuresData.data) {
                    const { features: featuresList } = featuresData.data;

                    if (featuresList.includes("CONTENT_RATING")) {
                        const acceptedContentRatingsData = await (await fetch(`https://api.casterlabs.co/v2/koi/stream/${platform}/ratings`)).json();

                        contentRatings[platform] = acceptedContentRatingsData.data.result;
                    }

                    features[platform] = featuresList; // Update this last.
                }
            })();
        }
    });
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
        <InputTitle {selectedAccount} {features} {currentInputData} />

        <InputCategory {selectedAccount} {features} {currentInputData} />

        <InputLanguage {selectedAccount} {features} {languages} {currentInputData} />

        <InputContentRating {selectedAccount} {features} {contentRatings} {currentInputData} />

        <!-- TODO TAGS & THUMBNAIL -->

        <a on:click={submit} class="button is-fullwidth" style="margin-top: 25px;">
            <img class="nav-icon" src="/img/services/{selectedAccount.userData.platform.toLowerCase()}/icon.svg" alt={selectedAccount.userData.platform} />
            &nbsp;&nbsp;
            <LocalizedText key="stream.update_info" />
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

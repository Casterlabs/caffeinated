<script>
    import { onDestroy, onMount } from "svelte";

    const unregister = [];

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import InputTitle from "./input-title.svelte";
    import InputCategory from "./input-category.svelte";
    import InputLanguage from "./input-language.svelte";
    import InputContentRating from "./input-content-rating.svelte";
    import InputThumbnail from "./input-thumbnail.svelte";

    const PLATFORMS = ["CAFFEINE", "TROVO"]; // The only ones supported.

    let accounts = [];

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
            thumbnailUrl: null,
            hasBeenModified: false
        };
        features[platform] = [];
        contentRatings[platform] = [];
    }

    export function render(newAccounts) {
        if (newAccounts) accounts = newAccounts;

        accounts = accounts
            .filter((account) => account.userData)
            .filter((account) => account.streamData)
            .filter((account) => PLATFORMS.includes(account.userData.platform));

        if (!selectedAccount && accounts[0]) {
            select(accounts[0]);
        }

        for (let account of accounts) {
            console.log(account.streamData);
            fillDefaults(account.userData.platform, account.streamData);
        }
    }

    function fillDefaults(platform, streamData) {
        const inputData = inputs[platform];

        if (!inputData.title) {
            inputData.title = streamData.title;
        }

        if (!inputData.category) {
            inputData.category = streamData.category;
        }

        if (!inputData.language && languages) {
            inputData.language = languages[streamData.language];
        }

        if (!inputData.contentRating) {
            inputData.contentRating = streamData.content_rating;
        }

        if (!inputData.tags) {
            inputData.tags = streamData.tags;
        }

        if (!inputData.thumbnailUrl) {
            inputData.thumbnailUrl = streamData.thumbnail_url;
        }
    }

    function select(account) {
        selectedAccount = account;
        currentPlatform = account.userData.platform;
        currentInputData = inputs[currentPlatform];
    }

    async function submit(platform = currentPlatform) {
        let inputData = inputs[currentPlatform];

        if (!inputData.hasBeenModified) {
            return;
        }

        let languageEnum = null;
        for (const [language, lang] of Object.entries(languages)) {
            if (lang == inputData.language) {
                languageEnum = language;
                break;
            }
        }

        let categoryId = null;
        {
            let query = inputData.category;

            if (query == "Entertainment" && platform == "CAFFEINE") {
                categoryId = "79";
            } else {
                // Lookup category id from name.
                // TODO Make this more error resitant.
                const response = await //
                (await fetch(`https://api.casterlabs.co/v2/koi/stream/${platform}/categories/search?q=${encodeURIComponent(query)}`)) //
                    .json();

                const result = Object.entries(response.data.result);

                console.log(result);
                categoryId = result[0][0];
            }
        }

        const streamUpdatePayload = {
            title: inputData.title,
            category: categoryId,
            language: languageEnum,
            tags: inputData.tags,
            content_rating: inputData.contentRating
        };

        console.debug("Update:", streamUpdatePayload, inputData.thumbnailFile);

        fetch("https://api.casterlabs.co/v2/koi/stream/update", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Client-ID": "LmHG2ux992BxqQ7w9RJrfhkW",
                Authorization: "Koi " + selectedAccount.token
            },
            body: JSON.stringify(streamUpdatePayload)
        });

        if (inputData.thumbnailFile) {
            fetch("https://api.casterlabs.co/v2/koi/stream/thumbnail/update", {
                method: "POST",
                headers: {
                    "Content-Type": "application/octet-stream",
                    "Client-ID": "LmHG2ux992BxqQ7w9RJrfhkW",
                    Authorization: "Koi " + selectedAccount.token
                },
                body: inputData.thumbnailFile
            });
        }

        // UI update.
        inputData.hasBeenModified = false;
    }

    /* ---------------- */
    /* Life Cycle   */
    /* ---------------- */

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

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

        unregister.push([
            "koi:event",

            Bridge.on("koi:event:stream_status", (e) => {
                console.debug("Got event!", e);

                const platform = e.streamer.platform;

                if (PLATFORMS.includes(platform)) {
                    const inputData = inputs[platform];

                    if (inputData.hasBeenModified) {
                        // Try to submit the changes as soon as the streamer goes live.
                        // This is for platforms like Caffeine which create a new broadcast
                        // api object which won't absorb the previous broadcast's info.
                        if (e.is_live) {
                            submit(platform);
                        }
                    } else {
                        // Grab some fresh defaults from the event.
                        fillDefaults(platform, e);
                    }
                }
            })
        ]);
    });
</script>

{#if accounts.length > 1}
    <nav class="pagination is-centered" aria-label="pagination">
        <!-- svelte-ignore a11y-missing-attribute -->
        <!-- svelte-ignore a11y-missing-content -->
        <ul class="pagination-list">
            {#each accounts as account}
                <li>
                    <a
                        on:click={() => select(account)}
                        class="pagination-link"
                        class:is-current={currentPlatform == account.userData.platform}
                        aria-label={account.userData.platform}
                    >
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

        <!-- TODO TAGS  -->

        <InputThumbnail {selectedAccount} {features} {currentInputData} />

        <a on:click={() => submit()} class="button is-fullwidth" style="margin-top: 25px;">
            <img class="nav-icon" src="/img/services/{selectedAccount.userData.platform.toLowerCase()}/icon.svg" alt={selectedAccount.userData.platform} />
            &nbsp;&nbsp;
            <LocalizedText key="stream.update_info" />
        </a>

        <br />
        <br />
        <br />
    </div>
{/if}

<style>
    .nav-icon {
        width: 24px;
        height: 24px;
        filter: invert(var(--white-invert-factor));
    }
</style>

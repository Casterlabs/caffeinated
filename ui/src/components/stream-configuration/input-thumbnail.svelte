<script>
    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import Aspect16by9 from "$lib/components/aspect-ratio/Aspect16by9.svelte";

    export let selectedAccount = null;
    export let features;

    export let currentInputData; // thumbnailUrl and thumbnailFile are our fields.

    let filePickerElement;
    let filePreview = null;

    let isReadingFile = false;

    function onPick(event) {
        isReadingFile = true;
        // Take the first file, convert to b64 and set currentInputData.thumbnailFile to the file.
        const file = filePickerElement.files[0];

        if (file) {
            const reader = new FileReader();

            reader.onload = () => {
                currentInputData.thumbnailFile = file;
                filePreview = reader.result;
                isReadingFile = false;
                markModified();
            };

            reader.onerror = () => {
                isReadingFile = false;
            };

            reader.readAsDataURL(file);
        }
    }

    function clear() {
        currentInputData.thumbnailFile = null;
        filePreview = null;
    }

    function markModified() {
        currentInputData.hasBeenModified = true;
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
{#if features[selectedAccount.userData.platform].includes("THUMBNAIL")}
    <div class="field">
        <label class="label">
            <LocalizedText key="stream.thumbnail" />
        </label>
        <div class="control">
            <Aspect16by9>
                <div id="thumbnail-container">
                    <img src={filePreview || currentInputData.thumbnailUrl} alt="Stream Preview" />

                    <span id="buttons">
                        <button class="button" class:hidden={!filePreview} id="clear-button" on:click={clear}>
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                class="feather feather-x"
                            >
                                <line x1="18" y1="6" x2="6" y2="18" />
                                <line x1="6" y1="6" x2="18" y2="18" />
                            </svg>
                        </button>
                        <button class="button" class:is-loading={isReadingFile} id="pick-button" aria-controls="#pick-file" on:click={() => filePickerElement.click()}>
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                class="feather feather-file-plus"
                            >
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                                <polyline points="14 2 14 8 20 8" />
                                <line x1="12" y1="18" x2="12" y2="12" />
                                <line x1="9" y1="15" x2="15" y2="15" />
                            </svg>
                        </button>

                        <input type="file" id="pick-file" aria-hidden="true" bind:this={filePickerElement} on:change={onPick} accept="image/png, image/gif, image/jpeg" />
                    </span>
                </div>
            </Aspect16by9>
        </div>
    </div>
{/if}

<style>
    #thumbnail-container {
        position: relative;
        width: 100%;
        height: 100%;
        background-color: #000000;
        background-size: cover;
        background-position: center;
        border-radius: 15px;
        overflow: hidden;
    }

    #thumbnail-container img::before {
        content: " ";
        display: block;
        position: absolute;
        height: 100%;
        width: 100%;
        background-color: black;
    }

    #buttons {
        position: absolute;
        bottom: 8px;
        right: 8px;
        text-align: right;
    }

    #buttons button {
        width: 30px;
        height: 30px;
        padding: 0;
        border-radius: 5px;
    }

    #buttons button svg {
        width: 18px;
        height: 18px;
    }

    #pick-file {
        display: none;
    }
</style>

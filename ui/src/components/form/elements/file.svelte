<script>
    export let widgetSettingsOption;
    export let value;

    const FILE_SIZE_THRESHOLD = 1048576 * 10; // 10mb

    import { createEventDispatcher } from "svelte";

    const allowedTypes = widgetSettingsOption.extraData.allowed;
    let blanking = false;

    const dispatch = createEventDispatcher();

    function getFileName() {
        if (value.includes("filename=")) {
            let filename = value;

            filename = filename.substring(filename.indexOf("filename=") + "filename=".length);
            filename = filename.substring(0, filename.indexOf(";"));

            return filename;
        } else {
            return "Unknown File Name";
        }
    }

    async function onChange({ target: fileInput }) {
        const file = fileInput.files[0];
        const result = await fileToBase64(file, allowedTypes);

        // console.log(result);

        value = result;

        recreateDom();

        dispatch("change", {
            value: value
        });
    }

    function clearInput() {
        value = "";

        recreateDom();

        dispatch("change", {
            value: value
        });
    }

    function recreateDom() {
        blanking = true;
        setTimeout(() => (blanking = false), 100); // Recreate the dom (clears the input)
    }

    function fileToBase64(file, allowedTypes) {
        return new Promise((resolve) => {
            // Check the file type.
            const filetype = file.type;

            if (allowedTypes) {
                let okay = false;

                for (const type of allowedTypes) {
                    if (filetype.startsWith(type)) {
                        okay = true;
                        break;
                    }
                }

                if (!okay) {
                    resolve("");
                    return;
                }
            }

            // Check the file size.
            const size = file.size;

            if (size > FILE_SIZE_THRESHOLD) {
                if (
                    confirm(
                        `The current selected file is greater than 10mb (Actual Size: ${fileSizeFormatter(
                            size,
                            1
                        )}) which is known to cause issues with Caffeinated.\n\nEither click OK to proceed or click cancel to select a smaller file.`
                    )
                ) {
                    console.debug("User OK'd a large file read.");
                } else {
                    resolve("");
                    fileElement.value = "";
                    console.debug("User aborted a large file read.");
                    return;
                }
            }

            console.debug(`Reading a ${fileSizeFormatter(size, 1)} file.`);

            try {
                const reader = new FileReader();

                reader.readAsDataURL(file);
                reader.onload = () => {
                    const filename = file.name;
                    let result = reader.result;

                    result = result.substring(result.indexOf(";") + 1);

                    // We sneak in our own filename property :^)
                    // God, I am so fucking big brained it hurts.
                    resolve(`data:${filetype};filename=${filename};${result}`);
                };
            } catch (e) {
                console.warn(e);
                resolve("");
                fileElement.value = "";
            }
        });
    }
</script>

{#if !blanking}
    <div class="field has-addons">
        <div class="control">
            <div class="file is-normal">
                <label class="file-label">
                    <input class="file-input" type="file" on:input={onChange} />
                    <span class="file-cta">
                        <span class="file-icon">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="24"
                                height="24"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                class="feather feather-file"
                            >
                                <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" />
                                <polyline points="13 2 13 9 20 9" />
                            </svg>
                        </span>
                        <span class="file-label">
                            {#if value}
                                {getFileName()}
                            {:else}
                                Choose a file...
                            {/if}
                        </span>
                    </span>
                </label>
            </div>
        </div>
        <p class="control">
            <button class="button" title="Clear" on:click={clearInput}>
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
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
        </p>
    </div>
{/if}

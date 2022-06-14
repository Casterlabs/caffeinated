<script>
    export let defaultValue;
    export let lookup = (value) => {
        return ["1", "2", "3"];
    };
    export let value;

    import { createEventDispatcher } from "svelte";

    let hasFocus = false;

    let searchOpen = false;
    let searchResults = [];

    let isLoading = false;
    let inputDebounce = 0;

    if (!value) {
        value = defaultValue || "";
    }

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            value: value
        });
    }

    function setValue(v) {
        value = v;
        searchOpen = false;
        onChange();
    }

    async function updateList(e) {
        const target = e.target;

        hasFocus = true;

        if (target instanceof HTMLInputElement) {
            target.type = "text"; // Hide the X
            isLoading = true;

            clearTimeout(inputDebounce);
            inputDebounce = setTimeout(async () => {
                searchResults = await lookup(value);
                searchOpen = true;
                isLoading = false;

                if (target instanceof HTMLInputElement) {
                    target.type = "search"; // Show the X
                }
            }, 300);
        } else {
            searchResults = await lookup(value);
            searchOpen = true;
        }
    }

    function onBlur() {
        hasFocus = false;

        setTimeout(() => {
            if (!hasFocus) {
                searchOpen = false;

                if (!searchResults.includes(value)) {
                    setValue(defaultValue);
                }
            }
        }, 300);
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div class="container">
    <!-- By switching between the elements we can give the user the illusion of this being 
        a proper dropdown whilst still retaining the functionality of an input -->
    {#if searchOpen}
        <!-- svelte-ignore a11y-autofocus -->
        <div class="control" class:is-loading={isLoading}>
            <input class="input" type="search" placeholder={defaultValue} bind:value on:input={updateList} on:focus={updateList} on:blur={onBlur} autofocus />
        </div>
    {:else}
        <div class="select" style="width: 100%;">
            <select on:focus={updateList} style="width: 100%;">
                <option selected>
                    {value || ""}
                </option>
            </select>
        </div>
    {/if}

    {#if searchOpen}
        <div id="search-result" class="box">
            <div tabindex="-1" on:focus={() => (hasFocus = true)}>
                <ul>
                    {#each searchResults as item}
                        <li>
                            <a class="highlight-on-hover" on:click={() => setValue(item)}> {item} </a>
                        </li>
                    {/each}
                </ul>
            </div>
        </div>
    {/if}
</div>

<style>
    .container {
        position: relative;
        width: 100%;
    }

    .input {
        z-index: 12;
    }

    .control .input::after {
        z-index: 15;
    }

    .select::after {
        border-width: 2.5px !important;
        top: 55% !important;
        right: 15px !important;
        width: 0.5em !important;
        height: 0.5em !important;
    }

    #search-result {
        position: absolute;
        padding: 0;
        top: 0;
        left: 0;
        width: 100%;
        height: 225px;
        z-index: 11;
    }

    #search-result > div {
        margin-top: 3em;
        height: calc(100% - 3em);
        overflow-x: hidden;
        overflow-y: auto;
    }

    #search-result > div > ul {
        padding-left: 1em;
        padding-right: 1em;
    }

    #search-result a {
        display: block;
        width: 100%;
    }
</style>

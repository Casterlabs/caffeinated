<script>
    export let defaultValue;
    export let entries = [];
    export let value;

    import { createEventDispatcher } from "svelte";

    const MAX_SEARCH_RESULTS = Number.MAX_SAFE_INTEGER;

    let hasFocus = false;

    let searchOpen = false;
    let searchResults = [];

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

    function updateList() {
        hasFocus = true;
        let newSearchResults = entries;

        newSearchResults = newSearchResults.filter((v) => {
            return v.toLowerCase().startsWith(value.toLowerCase());
        });

        if (newSearchResults.length > MAX_SEARCH_RESULTS) {
            newSearchResults.length = MAX_SEARCH_RESULTS;
        }

        searchResults = newSearchResults;
        searchOpen = true;
    }

    function onBlur() {
        hasFocus = false;

        setTimeout(() => {
            if (!hasFocus) {
                searchOpen = false;

                if (!entries.includes(value)) {
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
        <input class="input" type="search" placeholder={defaultValue} bind:value on:input={updateList} on:focus={updateList} on:blur={onBlur} autofocus />
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
                    {#each searchResults as font}
                        <li>
                            <a class="highlight-on-hover" on:click={() => setValue(font)}> {font} </a>
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

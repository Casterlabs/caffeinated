<script>
    export let widgetSettingsOption;
    export let value;

    import { createEventDispatcher, onMount } from "svelte";

    let currencies = [];

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            value: value
        });
    }

    onMount(async () => {
        currencies = (await Currencies.getCurrencies()).currencies;
    });
</script>

<div class="select">
    {#if currencies.length > 0}
        <select bind:value on:change={onChange}>
            {#if widgetSettingsOption.allowDefault}
                <option value="DEFAULT">Default</option>
            {/if}
            {#each currencies as currency}
                <option value={currency.currencyCode}>{currency.currencyName}</option>
            {/each}
        </select>
    {/if}
</div>

<style>
    .select {
        height: 1em;
    }

    .select::after {
        border-width: 2.5px !important;
        top: 55% !important;
        right: 15px !important;
        width: 0.5em !important;
        height: 0.5em !important;
    }

    select {
        font-size: 0.75em;
    }
</style>

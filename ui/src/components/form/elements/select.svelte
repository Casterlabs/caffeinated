<script>
    export let widgetSettingsOption;
    export let value;

    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            value: value
        });
    }
</script>

<div class="select">
    <select bind:value on:change={onChange}>
        {#each widgetSettingsOption.extraData.options as name}
            <option>{name}</option>
        {/each}
    </select>
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

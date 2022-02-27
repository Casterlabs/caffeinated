<script>
    export let widgetSettingsOption;
    export let value;

    import { createEventDispatcher } from "svelte";

    const { step, min, max } = widgetSettingsOption.extraData;

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            value: value
        });
    }

    function onInput() {
        dispatch("input", {
            value: value
        });
    }
</script>

<input class="range" type="range" bind:value on:change={onChange} on:input={onInput} {step} {min} {max} />

<style>
    .range {
        -webkit-appearance: none;
        height: 4px !important;
        transform: translateY(-3px);
        background-color: #adadad;
        --thumb-background: #e8e8e8;
        --thumb-color: #121212;
    }

    :global(.app-is-dark) .range {
        background-color: #5c5c5c;
        --thumb-background: #121212;
        --thumb-color: #e8e8e8;
    }

    .range::-webkit-slider-runnable-track {
        width: 300px;
        height: 2px;
        border: none;
        border-radius: 3px;
    }

    .range::-webkit-slider-thumb {
        -webkit-appearance: none;
        border: none;
        height: 12px;
        width: 12px;
        border-radius: 500%;
        background: var(--background-color);
        border: 2px solid var(--title-bar-text-color);
        margin-top: -5px;
        cursor: pointer;
    }
</style>

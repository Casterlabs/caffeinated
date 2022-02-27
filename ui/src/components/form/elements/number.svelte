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

<input class="number" type="number" bind:value on:change={onChange} on:input={onInput} {step} {min} {max} />

<style>
    .number {
        color: var(--text-color);
    }
</style>

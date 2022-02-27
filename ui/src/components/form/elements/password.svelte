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

    function onInput() {
        dispatch("input", {
            value: value
        });
    }
</script>

<input class="input" type="password" bind:value on:change={onChange} on:input={onInput} placeholder={widgetSettingsOption.extraData.placeholder} />

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

<textarea
    class="textarea"
    bind:value
    on:change={onChange}
    on:input={onInput}
    placeholder={widgetSettingsOption.extraData.placeholder}
    rows={widgetSettingsOption.extraData.rows || 4}
/>

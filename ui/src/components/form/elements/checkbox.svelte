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

<input class="checkbox" type="checkbox" bind:checked={value} on:change={onChange} />

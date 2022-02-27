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

    function highlightText(event) {
        const selection = window.getSelection();

        // Unhighlight text on second click.
        if (selection.anchorNode == event.target) {
            selection.removeAllRanges();
        } else {
            const range = document.createRange();
            range.selectNodeContents(event.target);
            selection.removeAllRanges();
            selection.addRange(range);
        }
    }
</script>

<input class="color" type="color" bind:value on:change={onChange} on:input={onInput} />
<span style="margin-left: 2px; user-select: text;" on:click={highlightText}>{value}</span>

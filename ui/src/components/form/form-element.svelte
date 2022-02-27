<script>
    export let widget;
    export let widgetSettingsSection;
    export let widgetSettingsOption;

    import { onMount } from "svelte";

    import FormCheckbox from "./elements/checkbox.svelte";
    import FormColor from "./elements/color.svelte";
    import FormNumber from "./elements/number.svelte";
    import FormSelect from "./elements/select.svelte";
    import FormText from "./elements/text.svelte";
    import FormTextArea from "./elements/textarea.svelte";
    import FormPassword from "./elements/password.svelte";
    import FormCurrency from "./elements/currency.svelte";
    import FormFont from "./elements/font.svelte";
    import FormRange from "./elements/range.svelte";
    import FormFile from "./elements/file.svelte";
    // import FormDynamic from "./elements/dynamic.svelte";
    // import FormSearch from "./elements/search.svelte";
    // import FormIframe from "./elements/iframe.svelte";

    let ElementClass;

    const type = widgetSettingsOption.type.toLowerCase();
    const settingsKey = `${widgetSettingsSection.id}.${widgetSettingsOption.id}`;

    let value = widget.settings[settingsKey];
    let inputDebounce = -1;

    if (value == undefined || value == null) {
        value = widgetSettingsOption.extraData.defaultValue;
    }

    // console.debug("[FormElement]", settingsKey, value);

    function onInput() {
        if (inputDebounce == -1) {
            inputDebounce = setTimeout(onChange, 275);
        }
    }

    async function onChange() {
        clearTimeout(inputDebounce);
        inputDebounce = -1;

        Bridge.emit("plugins:edit-widget-settings", {
            id: widget.id,
            key: settingsKey,
            newValue: value
        });
    }

    // Avert your eyes, children!
    switch (type) {
        case "checkbox": {
            ElementClass = FormCheckbox;
            break;
        }

        case "color": {
            ElementClass = FormColor;
            break;
        }

        case "number": {
            ElementClass = FormNumber;
            break;
        }

        case "dropdown": {
            ElementClass = FormSelect;
            break;
        }

        case "text": {
            ElementClass = FormText;
            break;
        }

        case "textarea": {
            ElementClass = FormTextArea;
            break;
        }

        case "password": {
            ElementClass = FormPassword;
            break;
        }

        case "currency": {
            ElementClass = FormCurrency;
            break;
        }

        case "font": {
            ElementClass = FormFont;
            break;
        }

        case "range": {
            ElementClass = FormRange;
            break;
        }

        case "file": {
            ElementClass = FormFile;
            break;
        }
    }
</script>

{#if !ElementClass}
    {#if type == "html"}
        {@html widgetSettingsOption.extraData.html}
    {:else}
        ... {type}
    {/if}
{:else}
    <svelte:component this={ElementClass} {widget} {settingsKey} {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
{/if}

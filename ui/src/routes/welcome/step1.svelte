<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import { supportedLanguages } from "$lib/translate.mjs";

    let uiPreferences = {};

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(async () => {
        uiPreferences = await Caffeinated.UI.preferences;

        document.title = "";
        console.log(supportedLanguages);
        plausible("Fresh Install");
    });

    async function updateLanguage() {
        Caffeinated.UI.updateAppearance({
            ...uiPreferences,
            theme: (await Caffeinated.themeManager.currentTheme).id
        });
    }
</script>

<div class="has-text-centered">
    <br />
    <br />
    <br />
    <h1 class="title is-2">
        <LocalizedText key="ui.greeting.hello" />
    </h1>
    <br />
    <br />
    <h1 class="title is-4">
        <LocalizedText key="ui.welcome" />
    </h1>
    <h2 class="subtitle is-6">
        <LocalizedText key="ui.welcome.sub" />
    </h2>
    <br />
    <br />
    <br />
    <br />
    <div class="field has-addons" style="width: fit-content; margin: auto;">
        <div class="control">
            <div class="select">
                <select bind:value={uiPreferences.language} on:change={updateLanguage}>
                    {#each supportedLanguages as language}
                        <option value={language.code}>
                            {language.flag}
                        </option>
                    {/each}
                </select>
            </div>
        </div>
        <div class="control">
            <a class="button" href="/welcome/step2">
                <LocalizedText key="ui.welcome.start" />
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-arrow-right"><line x1="5" y1="12" x2="19" y2="12" /><polyline points="12 5 19 12 12 19" /></svg
                >
            </a>
        </div>
    </div>
</div>

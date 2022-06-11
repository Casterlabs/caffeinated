<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import EmojiText from "$lib/components/EmojiText.svelte";
    import { supportedLanguages } from "$lib/translate.mjs";

    let dropdownOpen = false;
    let uiPreferences = {};

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(async () => {
        uiPreferences = await Caffeinated.UI.preferences;

        document.title = "";
        console.log(supportedLanguages);
    });

    async function updateLanguage(language) {
        dropdownOpen = false;
        uiPreferences.language = language.code;

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
            <div class="dropdown" class:is-active={dropdownOpen}>
                <div class="dropdown-trigger">
                    <button class="button" aria-haspopup="true" aria-controls="language-dropdown" on:click={() => (dropdownOpen = !dropdownOpen)}>
                        <span>
                            {#each supportedLanguages as language}
                                {#if language.code == uiPreferences.language}
                                    <EmojiText forceProvider="twemoji">
                                        {language.flag}
                                    </EmojiText>
                                {/if}
                            {/each}
                        </span>
                        <span class="icon is-small">
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="20"
                                height="20"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                class="feather feather-chevron-down"
                                style="margin-top: 2px;"
                            >
                                <polyline points="6 9 12 15 18 9" />
                            </svg>
                        </span>
                    </button>
                </div>
                <div class="dropdown-menu" id="language-dropdown" role="menu">
                    <div class="dropdown-content" style="width: 69px; height: 125px; overflow-y: auto;">
                        {#each supportedLanguages as language}
                            <!-- svelte-ignore a11y-missing-attribute -->
                            <a class="dropdown-item" on:click={() => updateLanguage(language)} style="padding-left: 0; padding-right: 0;">
                                <EmojiText forceProvider="twemoji">
                                    {language.flag}
                                </EmojiText>
                            </a>
                        {/each}
                    </div>
                </div>
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

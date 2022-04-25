<script>
    // Meta
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    // Settings Content
    import SettingsContainer from "../../components/settings/settings-container.svelte";
    import AppearanceSettings from "../../components/settings/appearance-settings.svelte";
    import MusicServicesSettings from "../../components/settings/music-services-settings.svelte";
    // import StatusIntegrationSettings from "../../components/settings/status-integration-settings.svelte";

    const settingsContainerContents = [
        {
            name: "settings.app",
            type: "section"
        },
        {
            name: "settings.appearance",
            type: "category",
            component: AppearanceSettings
        },
        {
            name: "settings.accounts",
            type: "section"
        },
        {
            name: "settings.music_services",
            type: "category",
            component: MusicServicesSettings
        }
        // {
        //     name: "Other",
        //     type: "section"
        // },
        // {
        //     name: "Status Integration",
        //     id: "status_integration",
        //     type: "category",
        //     component: StatusIntegrationSettings
        // }
    ];

    onMount(() => {
        document.title = "";
    });
</script>

<div class="welcome-contents">
    <div class="welcome-title has-text-centered">
        <h1 class="title is-4">
            <LocalizedText key="ui.welcome.customize" />
        </h1>
    </div>

    <div class="welcome-settings">
        <SettingsContainer categories={settingsContainerContents} updateTitle={false} />
    </div>

    <a id="next-page-button" class="button" href="/signin">
        <LocalizedText key="generic.next" />
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

<style>
    .welcome-settings {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    .welcome-title {
        margin-top: 3.5em;
        margin-left: 175px;
    }

    /* Pad out the settings page */
    :global(.settings-navigate) {
        padding-top: 9em;
    }

    :global(.settings-content) {
        top: 9em !important;
    }

    #next-page-button {
        position: absolute;
        bottom: 1em;
        right: 1em;
    }
</style>

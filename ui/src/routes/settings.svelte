<script>
    // Meta
    import { setPageProperties } from "./__layout.svelte";

    import SettingsContainer from "../components/settings/settings-container.svelte";

    // Settings Content
    import StreamingServicesSettings from "../components/settings/streaming-services-settings.svelte";
    import MusicServicesSettings from "../components/settings/music-services-settings.svelte";
    import AppearanceSettings from "../components/settings/appearance-settings.svelte";
    // import StatusIntegrationSettings from "../components/settings/status-integration-settings.svelte";
    import AboutSettings from "../components/settings/about-settings.svelte";
    import DeveloperStuff from "../components/settings/developer-stuff.svelte";
    import PairADevice from "../components/settings/pair-a-device.svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: true
    });

    const settingsContainerContents = [
        {
            name: "App",
            type: "section"
        },
        {
            name: "Appearance",
            type: "category",
            component: AppearanceSettings
        },
        {
            name: "About",
            type: "category",
            component: AboutSettings
        },
        {
            name: "Developer Stuff",
            type: "category",
            component: DeveloperStuff
        },
        {
            name: "Accounts",
            type: "section"
        },
        {
            name: "Streaming Services",
            type: "category",
            component: StreamingServicesSettings
        },
        {
            name: "Music Services",
            type: "category",
            component: MusicServicesSettings
        },
        {
            name: "Pair a Device",
            type: "category",
            component: PairADevice
        }
        // {
        //     name: "Status Integration",
        //     id: "status_integration",
        //     type: "category",
        //     component: StatusIntegrationSettings
        // }
    ];
</script>

<div class="settings-contents">
    <!-- svelte-ignore a11y-missing-attribute -->
    <a class="settings-close fade-on-hover" onclick="history.back()">
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
            class="feather feather-x-circle"
        >
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" />
            <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
    </a>

    <div class="settings-body">
        <SettingsContainer categories={settingsContainerContents} />
    </div>
</div>

<style>
    .settings-close {
        position: absolute;
        top: 0.75em;
        right: 0.75em;
        color: inherit;
        z-index: 2;
    }

    .settings-close svg {
        height: 28px;
        width: 28px;
    }

    /* Pad out the settings page */
    :global(.settings-navigate) {
        padding-top: 2em;
    }

    :global(.settings-content) {
        top: 2em !important;
    }
</style>

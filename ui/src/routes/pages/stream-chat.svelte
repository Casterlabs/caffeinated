<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatViewer from "$lib/components/chat/chat-viewer.svelte";

    import { onMount, onDestroy } from "svelte";

    const unregister = [];

    let viewerElement = {};

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    /* ---------------- */
    /* Bridge Events    */
    /* ---------------- */

    function bridge_processEvent(event) {
        viewerElement.processEvent(event);
    }

    function bridge_onAuthUpdate(data) {
        viewerElement.onAuthUpdate(Object.keys(data));
    }

    function bridge_onChatViewerPreferencesUpdate(data) {
        viewerElement.loadConfig(data);
    }

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onSavePreferences({ detail: data }) {
        Caffeinated.UI.chatPreferences = data;
    }

    function onChatSend({ detail: data }) {
        const { platform, message } = data;

        Caffeinated.koi.sendChat(platform, message, "CLIENT");
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        function sendCommand(command) {
            Caffeinated.koi.sendChat(platform, command, "CLIENT");
        }

        switch (type) {
            case "ban": {
                switch (platform) {
                    case "TWITCH": {
                        sendCommand(`/ban ${event.sender.username}`);
                        return;
                    }

                    case "TROVO": {
                        sendCommand(`/ban ${event.sender.username}`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "timeout": {
                // We timeout for 10 minutes
                switch (platform) {
                    case "TWITCH": {
                        sendCommand(`/timeout ${event.sender.username} 600`);
                        return;
                    }

                    case "TROVO": {
                        sendCommand(`/ban ${event.sender.username} 600`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "delete": {
                if (["TWITCH", "BRIME", "TROVO"].includes(platform)) {
                    Caffeinated.koi.upvoteChat(platform, event.id);
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    Caffeinated.koi.upvoteChat(platform, event.id);
                }
                return;
            }

            case "raid": {
                switch (platform) {
                    case "TWITCH": {
                        sendCommand(`/raid ${event.sender.username}`);
                        return;
                    }

                    case "CAFFEINE": {
                        sendCommand(`/afterparty ${event.sender.username}`);
                        return;
                    }

                    case "TROVO": {
                        sendCommand(`/host ${event.sender.username}`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }
        }
    }

    /* ---------------- */
    /* Life Cycle   */
    /* ---------------- */

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - Stream Chat";

        bridge_onAuthUpdate(await Caffeinated.auth.authInstances);
        bridge_onChatViewerPreferencesUpdate(await Caffeinated.UI.chatPreferences);

        unregister.push(["koi:event", Bridge.on("koi:event", bridge_processEvent)]);
        (await Caffeinated.koi.eventHistory).forEach(bridge_processEvent);

        for (const [platform, viewers] of Object.entries(await Caffeinated.koi.viewers)) {
            bridge_processEvent({
                streamer: {
                    platform: platform
                },
                viewers: viewers,
                event_type: "VIEWER_LIST"
            });
        }
    });
</script>

<ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} on:modaction={onModAction} on:savepreferences={onSavePreferences} />

<!-- svelte-ignore a11y-missing-attribute -->
<div id="popout-buttons">
    <!-- <a onclick="Bridge.emit('ui:popout-chat');">
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="feather feather-external-link"
        >
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
            <polyline points="15 3 21 3 21 9" />
            <line x1="10" y1="14" x2="21" y2="3" />
        </svg>
    </a> -->

    <!-- <a onclick="Bridge.emit('ui:popout-viewers');">
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="feather feather-eye"
        >
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
            <circle cx="12" cy="12" r="3" />
        </svg>
    </a> -->
</div>

<style>
    #popout-buttons {
        position: absolute;
        top: 0.25em;
        right: 0.25em;
    }

    #popout-buttons a {
        color: var(--text-color);
        vertical-align: middle;
    }
</style>

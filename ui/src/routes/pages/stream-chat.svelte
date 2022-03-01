<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatViewer from "$lib/components/chat/chat-viewer.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;
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
        viewerElement.onAuthUpdate(data);
    }

    function bridge_onChatViewerPreferencesUpdate(data) {
        viewerElement.loadConfig(data);
    }

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onSavePreferences({ detail: data }) {
        Bridge.emit("ui:save_chat_viewer_preferences", {
            preferences: data
        });
    }

    function onChatSend({ detail: data }) {
        Bridge.emit("koi:chat_send", data);
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        function sendCommand(command) {
            Bridge.emit("koi:chat_send", {
                message: command,
                platform: platform
            });
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
                    Bridge.emit("koi:chat_delete", {
                        messageId: event.id,
                        platform: platform
                    });
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    Bridge.emit("koi:chat_upvote", {
                        messageId: event.id,
                        platform: platform
                    });
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
        eventHandler?.destroy();
    });

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - Stream Chat";

        eventHandler = Bridge.createThrowawayEventHandler();

        eventHandler.on("auth:update", bridge_onAuthUpdate);
        bridge_onAuthUpdate((await Bridge.query("auth")).data);

        eventHandler.on("ui:chatViewerPreferences:update", bridge_onChatViewerPreferencesUpdate);
        bridge_onChatViewerPreferencesUpdate((await Bridge.query("ui:chatViewerPreferences")).data);

        eventHandler.on("koi:event", bridge_processEvent);
        (await Bridge.query("koi:history")).data.forEach(bridge_processEvent);

        for (const [platform, viewers] of Object.entries((await Bridge.query("koi:viewers")).data)) {
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

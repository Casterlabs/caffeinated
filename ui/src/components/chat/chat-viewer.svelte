<script>
    import * as CommandPaletteGenerator from "./commandPaletteGenerator.js";
    import ChatMessage from "./chat-message.svelte";
    import Viewers from "./viewers.svelte";

    import { createEventDispatcher, onMount } from "svelte";

    const dispatch = createEventDispatcher();

    const DISPLAYABLE_EVENTS = ["FOLLOW", "CHAT", "DONATION", "SUBSCRIPTION", /*"VIEWER_JOIN",*/ /*"VIEWER_LEAVE",*/ "RAID", "CHANNEL_POINTS", "CLEARCHAT", "PLATFORM_MESSAGE"];

    let chatHistory = {};
    let isMultiPlatform = false;
    let chatboxContainer;
    let chatbox;
    let chatInput;
    let signedInPlatforms = [];
    let showJumpToBottomButton = false;

    let blurBackground = false;

    let showChatSettings = false;

    let chatSendPlatform = "TWITCH";
    let chatSendPlatformOpen = false;
    let chatSendMessage = "";

    let showCommandPalette = false;
    let selectedCommandIndex = 0;
    let commandPalette = [];
    let maxCommandIndex = 0;

    let viewersListElement = null;

    // Preferences
    let showChatTimestamps = true;
    let showModActions = true;
    let showProfilePictures = false;
    let showBadges = false;
    let showViewers = false;
    let showViewersList = false;

    function onMeta(event) {
        const chatElement = chatHistory[event.id];

        chatElement.upvotes = event.upvotes;
        chatElement.isDeleted = !event.is_visible;
    }

    function savePreferences() {
        dispatch("savepreferences", {
            showChatTimestamps: showChatTimestamps,
            showModActions: showModActions,
            showProfilePictures: showProfilePictures,
            showBadges: showBadges,
            showViewers: showViewers,
            showViewersList: showViewersList
        });
    }

    export function loadConfig(config) {
        showChatTimestamps = config.showChatTimestamps;
        showModActions = config.showModActions;
        showProfilePictures = config.showProfilePictures;
        showBadges = config.showBadges;
        showViewers = config.showViewers;
        showViewersList = config.showViewersList;
    }

    // TODO better processing of messages, this works for now.
    export function processEvent(event) {
        if (event.event_type == "VIEWER_LIST") {
            viewersListElement.onViewersList(event);
        } else if (event.event_type == "META") {
            onMeta(event);
        }

        // This means that we must delete ALL of the messages from a user.
        else if (event.event_type == "CLEARCHAT" && event.user_upid) {
            for (const chatMessage of Object.values(chatHistory)) {
                const koiEvent = chatMessage.koiEvent;

                if (koiEvent.sender && koiEvent.sender.UPID == event.user_upid) {
                    chatMessage.isDeleted = true;
                }
            }
        }

        // Display the chat message.
        else if (DISPLAYABLE_EVENTS.includes(event.event_type)) {
            // Mark old messages as deleted.
            // We do still need to create the message to let the user know chat was cleared.
            if (event.event_type == "CLEARCHAT") {
                const now = Date.now();

                for (const chatMessage of Object.values(chatHistory)) {
                    if (chatMessage.timestamp < now) {
                        chatMessage.isDeleted = true;
                    }
                }
            }

            const elem = document.createElement("li");

            const message = new ChatMessage({
                target: elem,
                props: {
                    koiEvent: event,
                    modAction: (type, event) => {
                        dispatch("modaction", {
                            type: type,
                            event: event
                        });
                    }
                }
            });

            if (event.id) {
                // This event is editable in some way, shape, or form.
                // (so, we must keep track of it)
                chatHistory[event.id] = message;
            }

            chatbox.appendChild(elem);
        } else {
            return;
        }

        tryScroll();
        console.log("[ChatViewer]", "Processed event:", event);
    }

    function generateCommandPalette() {
        let commandSections = CommandPaletteGenerator.generate(signedInPlatforms);

        // Generate the indexes.
        let idx = 0;
        for (const section of commandSections) {
            for (const command of section.commands) {
                command.index = idx;
                command.platform = section.platform;
                idx++;
            }
        }

        maxCommandIndex = idx - 1;

        // Filter the command sections (we're keeping the ids the same.)
        // If the command is roughly equal to the input we keep it
        // e.g "/em" would keep "/emoteonly" and "/emoteonlyoff"
        let filteredCommandSections = [];
        let filteredCommandIndexes = [];

        for (const section of commandSections) {
            const filteredSection = {
                ...section,
                commands: []
            };

            for (const command of section.commands) {
                if (command.command.startsWith(chatSendMessage)) {
                    filteredSection.commands.push(command);
                    filteredCommandIndexes.push(command.index);
                }
            }

            if (filteredSection.commands.length > 0) {
                filteredCommandSections.push(filteredSection);
            }
        }

        // Additionally, if there isn't a command that matches then we select the first one.
        if (!filteredCommandIndexes.includes(selectedCommandIndex)) {
            selectedCommandIndex = filteredCommandIndexes[0];
        }

        return filteredCommandSections;
    }

    function tryScroll() {
        const jumpingToBottom = isNearBottom();

        if (jumpingToBottom) {
            jumpToBottom();
        } else {
            showJumpToBottomButton = true;
        }
    }

    function isNearBottom() {
        const scrollPercent = (chatboxContainer.scrollTop + chatboxContainer.clientHeight) / chatboxContainer.scrollHeight;
        return scrollPercent >= 0.9;
    }

    function jumpToBottom() {
        showJumpToBottomButton = false;
        chatboxContainer.scroll(0, chatboxContainer.scrollHeight);
    }

    function commandPaletteListener(e) {
        if (showCommandPalette) {
            // These keys are used to navigate the command palette.
            if (e.key == "ArrowUp") {
                e.preventDefault();
                selectedCommandIndex = selectedCommandIndex < 0 ? maxCommandIndex : selectedCommandIndex - 1;
                return;
            } else if (e.key == "ArrowDown") {
                e.preventDefault();
                selectedCommandIndex = (selectedCommandIndex + 1) % maxCommandIndex;
                return;
            } else if (e.key == "Escape") {
                // No selection.
                selectedCommandIndex = -1;
                e.preventDefault();
                return;
            } else if (e.key == "Backspace") {
                sendChatMessage(e); // Bug (?)
            } else if (e.key == "Enter" || e.key == "Tab") {
                // Auto-complete the command.
                if (selectedCommandIndex != -1) {
                    const currentCommand = getSelectedCurrentCommand();

                    if (!currentCommand) {
                        // We go ahead and send it anyways.
                        sendChatMessage();
                    } else {
                        const hasCommandFilled = chatSendMessage.startsWith(currentCommand.command);

                        if (hasCommandFilled) {
                            // If they completed a command and it's still filled
                            // we shouldn't try to complete it again and should just send.
                            sendChatMessage();
                        } else {
                            e.preventDefault();
                            completeCommandPalette(selectedCommandIndex);
                            return;
                        }
                    }
                }
            }
        }
    }

    function sendChatMessage(e) {
        // Timeout is required for the binds to be updated.
        setTimeout(() => {
            // The keyboard event is from the input itself.
            if (e instanceof KeyboardEvent) {
                if (e.key == "Enter") {
                    // If the user presses enter then we should send the message and clear everything.
                    showCommandPalette = false;
                    selectedCommandIndex = -1;

                    // Fall out of the if statement.
                } else {
                    // Shows the commands popup when you start your message with '/'.
                    showCommandPalette = chatSendMessage.startsWith("/"); // TODO Fix.

                    // Reset the index.
                    if (showCommandPalette) {
                        commandPalette = generateCommandPalette();
                    } else {
                        selectedCommandIndex = -1;
                    }

                    // The input was NOT a signal to send, so we return.
                    return;
                }
            }

            // We don't want to send empty messages.
            if (chatSendMessage.length > 0) {
                dispatch("chatsend", {
                    platform: chatSendPlatform,
                    message: chatSendMessage
                });

                console.log("[ChatViewer]", "Sending chat message:", chatSendPlatform, ">", chatSendMessage);
                chatSendMessage = "";

                chatInput.blur();
            }
        }, 0);
    }

    function changeSendPlatform(platform) {
        chatSendPlatform = platform;
        chatSendPlatformOpen = false;
    }

    function toggleChatSendPlatformDropdown() {
        chatSendPlatformOpen = !chatSendPlatformOpen;
    }

    function toggleChatSettings() {
        showChatSettings = !showChatSettings;
    }

    function completeCommandPalette(command) {
        if (typeof command == "number") {
            completeCommandPalette(getSelectedCurrentCommand());
        } else {
            // Update the platform.
            chatSendPlatform = command.platform;

            // Set the input to the command + a space.
            chatSendMessage = command.command + " ";

            // Close the palette.
            commandPalette = [];
        }
    }

    function getSelectedCurrentCommand() {
        // Loop through all the commands and find the one with the correct index.
        for (const section of commandPalette) {
            for (const c of section.commands) {
                if (c.index == selectedCommandIndex) {
                    return c;
                }
            }
        }
    }

    export function onAuthUpdate({ koiAuth }) {
        signedInPlatforms = Object.keys(koiAuth);
        isMultiPlatform = signedInPlatforms.length > 1;

        if (!signedInPlatforms.includes(chatSendPlatform)) {
            chatSendPlatform = signedInPlatforms[0];
        }

        viewersListElement.onAuthUpdate(signedInPlatforms);
    }

    onMount(() => {
        chatboxContainer.addEventListener("scroll", () => {
            showJumpToBottomButton = !isNearBottom();
        });
    });
</script>

{#if blurBackground}
    <div id="background-cover" />
{/if}

<div
    class="stream-chat-container 

        {showChatTimestamps ? 'show-timestamps' : ''} 
        {showModActions ? 'enable-mod-actions' : ''} 
        {showProfilePictures ? 'show-profile-pictures' : ''} 
        {showBadges ? 'show-badges' : ''} 

        {showChatSettings ? 'chat-settings-open' : ''} 
        {isMultiPlatform ? 'is-multi-platform' : ''} 
        {showCommandPalette && commandPalette.length > 0 ? 'chat-command-palette-open' : ''} 
    "
>
    <div id="chat-box" bind:this={chatboxContainer}>
        <ul bind:this={chatbox} />
    </div>

    <!-- svelte-ignore a11y-missing-attribute -->
    <a class="jump-button" on:click={jumpToBottom} style="opacity: {showJumpToBottomButton ? 1 : 0};">
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
            class="feather feather-arrow-down"
        >
            <line x1="12" y1="5" x2="12" y2="19" />
            <polyline points="19 12 12 19 5 12" />
        </svg>
    </a>

    <div id="viewers-list" style="display: {showViewersList ? 'block' : 'none'}">
        <Viewers bind:this={viewersListElement} />
    </div>

    <div id="chat-settings" class="box">
        <span class="is-size-6" style="font-weight: 700;"> Chat Preferences </span>
        <div>
            <label class="checkbox">
                <span class="label-text">Show Timestamps</span>
                <input type="checkbox" bind:checked={showChatTimestamps} on:change={savePreferences} />
            </label>
            <label class="checkbox">
                <span class="label-text">Show Mod Actions</span>
                <input type="checkbox" bind:checked={showModActions} on:change={savePreferences} />
            </label>
            <label class="checkbox">
                <span class="label-text">Show Avatars</span>
                <input type="checkbox" bind:checked={showProfilePictures} on:change={savePreferences} />
            </label>
            <label class="checkbox">
                <span class="label-text">Show Badges</span>
                <input type="checkbox" bind:checked={showBadges} on:change={savePreferences} />
            </label>
            <!-- <label class="checkbox">
                <span class="label-text">Show Viewers</span>
                <input type="checkbox" bind:checked={showViewers} on:change={savePreferences} />
            </label> -->
            <label class="checkbox">
                <span class="label-text">Show Viewers List</span>
                <input type="checkbox" bind:checked={showViewersList} on:change={savePreferences} />
            </label>
        </div>
    </div>

    <div id="chat-command-palette" class="box">
        {#each commandPalette as commandSection}
            <div class="command-section">
                <h1 class="title is-size-6 is-light" style="margin-left: 8px; margin-bottom: 7px; font-weight: 700;">
                    {commandSection.platform}
                </h1>
                {#each commandSection.commands as command}
                    <div
                        class="command {selectedCommandIndex == command.index ? 'highlight' : ''}"
                        style="padding-left: 8px; padding-bottom: 4px; border-radius: 3px; cursor: pointer;"
                        on:click={() => completeCommandPalette(command)}
                        on:mouseenter={() => (selectedCommandIndex = command.index)}
                    >
                        <span class="command-name">
                            <span class="command-name-text is-size-6" style="font-weight: 500;">
                                {command.command}
                            </span>
                            {#if command.args}
                                {#each command.args as arg}
                                    <span class="command-name-arg">{arg}</span>&nbsp;
                                {/each}
                            {/if}
                        </span>
                        <span class="command-description">
                            <h2 class="subtitle is-size-7 is-light">{command.description}</h2>
                        </span>
                    </div>
                {/each}
            </div>
        {/each}
    </div>

    <div class="interact-box-container">
        <div id="interact-box">
            <div class="field has-addons">
                {#if isMultiPlatform}
                    <div class="control" style="width: 50px;">
                        <div class="dropdown is-up {chatSendPlatformOpen ? 'is-active' : ''}">
                            <div class="dropdown-trigger">
                                <button class="button" aria-haspopup="true" aria-controls="chat-send-platform" on:click={toggleChatSendPlatformDropdown}>
                                    <span>
                                        <img
                                            src="/img/services/{chatSendPlatform.toLowerCase()}/icon.svg"
                                            alt={chatSendPlatform}
                                            style="height: 18px; width: 18px; filter: invert(var(--white-invert-factor)); margin-top: 8px;"
                                        />
                                    </span>
                                </button>
                            </div>
                            <div class="dropdown-menu" id="chat-send-platform" role="menu">
                                <div class="dropdown-content" style="width: 50px;">
                                    {#each signedInPlatforms as platform}
                                        <!-- svelte-ignore a11y-missing-attribute -->
                                        <a class="highlight-on-hover is-block" style="height: 30px;" on:click={() => changeSendPlatform(platform)}>
                                            <div class="dropdown-item">
                                                <img
                                                    src="/img/services/{platform.toLowerCase()}/icon.svg"
                                                    alt={platform}
                                                    style="height: 18px; width: 18px; filter: invert(var(--white-invert-factor));"
                                                />
                                            </div>
                                        </a>
                                    {/each}
                                </div>
                            </div>
                        </div>
                    </div>
                {/if}
                <div class="control is-expanded" style="position: relative;">
                    <input
                        class="input"
                        type="text"
                        placeholder="Send a message"
                        bind:this={chatInput}
                        bind:value={chatSendMessage}
                        on:keydown={commandPaletteListener}
                        on:keypress={sendChatMessage}
                        on:touchstart={(e) => {
                            // When a mobile device touches the input, move it near the top of the screen
                            e.preventDefault();
                            e.target.style = "position: fixed; top: 15vh; left: 1em; width: calc(100% - 2em); z-index: 200 !important;";
                            e.target.focus();
                            blurBackground = true;
                        }}
                        on:blur={(e) => {
                            // When the input loses focus, move it back to its original position
                            e.target.style = "";
                            blurBackground = false;
                        }}
                    />

                    <!-- svelte-ignore a11y-missing-attribute -->
                    <a class="chat-settings-button heavy-highlight-on-hover" on:click={toggleChatSettings}>
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="white"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            class="feather feather-settings"
                        >
                            <circle cx="12" cy="12" r="3" />
                            <path
                                d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"
                            />
                        </svg>
                    </a>
                </div>
                <div class="control">
                    <button class="button" on:click={sendChatMessage}> Send </button>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    :root {
        --interact-height: 40.5px;
        --interact-margin: 15px;
    }

    #background-cover {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
    }

    .stream-chat-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    #chat-box {
        position: absolute;
        top: 0;
        bottom: calc(var(--interact-margin) + var(--interact-height) + var(--interact-margin));
        left: 0;
        right: 0;
        font-size: 1.05em;
        padding-top: 10px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    .stream-chat-container::before {
        content: "";
        position: absolute;
        top: -10px;
        left: 0;
        width: 100%;
        height: 10px;
        box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.05);
        z-index: 5;
    }

    .stream-chat-container::after {
        content: "";
        position: absolute;
        bottom: calc(var(--interact-margin) + var(--interact-height) + var(--interact-margin) - 10px);
        left: 0;
        width: 100%;
        height: 10px;
        box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.3);
        z-index: 5;
    }

    .interact-box-container {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: calc(var(--interact-margin) + var(--interact-height) + var(--interact-margin));
        background-color: var(--background-color);
        z-index: 10;
    }

    #interact-box {
        position: absolute;
        bottom: var(--interact-margin);
        left: var(--interact-margin);
        right: var(--interact-margin);
        height: var(--interact-height);
    }

    /* Chat settings */

    .chat-settings-button {
        position: absolute;
        top: 5px;
        right: 10px;
        z-index: 20;
        width: 30px;
        height: 30px;
        transition: 0.15s;
        border-radius: 5px;
        filter: invert(var(--white-invert-factor));
    }

    .chat-settings-button svg {
        height: 20px;
        width: 20px;
        margin-left: calc(50% - 10px);
        margin-top: calc(50% - 10px);
        transition: 0.35s;
    }

    .chat-settings-open .chat-settings-button svg {
        transform: rotate(45deg);
    }

    #chat-settings {
        position: absolute;
        right: 20px;
        bottom: calc(var(--interact-margin) + var(--interact-height));
        height: 0px;
        width: 13em;
        visibility: hidden;
        opacity: 0;
        transition: 0.35s;
        overflow: hidden;
    }

    .chat-settings-open #chat-settings {
        visibility: visible;
        height: 195px;
        opacity: 1;
    }

    #chat-settings .label-text {
        float: left;
        width: 145px;
    }

    #chat-settings label {
        display: block;
        padding-bottom: 2px;
    }

    /* Viewers List */
    #viewers-list {
        position: absolute;
        top: 0;
        right: 0;
        width: 200px;
        height: 275px;
    }

    /* Command Palette */

    .highlight {
        background-color: var(--highlight-color) !important;
    }

    #chat-command-palette {
        position: absolute;
        left: 66px;
        right: 83px;
        bottom: calc(var(--interact-margin) + var(--interact-height));
        height: 0px;
        visibility: hidden;
        opacity: 0;
        overflow-y: auto;
        transition: 0.35s;
    }

    .command-section {
        margin-bottom: 10px !important;
    }

    .chat-command-palette-open #chat-command-palette {
        visibility: visible;
        height: 200px;
        opacity: 1;
    }

    /* Jump To Bottom */

    .jump-button {
        position: absolute;
        bottom: 5.5em;
        right: 1.5em;
        width: 32px;
        height: 32px;
        border-radius: 100%;
        color: black;
        background-color: white;
        transition: 0.15s;
    }

    .jump-button svg {
        width: 28px;
        height: 28px;
        margin-left: calc(50% - 14px);
        margin-top: calc(50% - 14px);
    }
</style>

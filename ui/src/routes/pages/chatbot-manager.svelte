<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    import translate from "$lib/translate.mjs";
    import App from "$lib/app.mjs";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    const PLATFORMS = [
        [null, "Any Platform"],
        ["CAFFEINE", "Caffeine"],
        ["TWITCH", "Twitch"],
        ["TROVO", "Trovo"],
        ["GLIMESH", "Glimesh"],
        ["BRIME", "Brime"]
    ];

    const SHOUT_EVENT_TYPES = ["DONATION", "FOLLOW", "RAID", "SUBSCRIPTION"];

    let currentTab = "COMMANDS";

    let mentionSenderInReply = true;
    let commands = [];
    let shouts = [];
    let timers = [];
    let timerInterval = 90;

    function updatePreferences() {
        const preferences = {
            mentionInReply: mentionSenderInReply,
            commands: commands,
            shouts: shouts,
            timers: timers,
            timerInterval: timerInterval,
            chatter: "CLIENT" // TODO
        };

        Caffeinated.chatbot.preferences = preferences;
        console.log(preferences);
    }

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - " + translate(App.get("language"), "chatbot_manager");

        const preferences = await Caffeinated.chatbot.preferences;

        commands = preferences.commands;
        shouts = preferences.shouts;
        timers = preferences.timers;
        timerInterval = preferences.timerIntervalSeconds;
        mentionSenderInReply = preferences.mentionInReply;
        // chatter = preferences.chatter;
    });
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div style="margin: 15px;" on:change={updatePreferences}>
    <div class="tabs">
        <ul style="justify-content: center !important;">
            {#each ["COMMANDS", "SHOUTS", "TIMERS"] as tab}
                {#if currentTab == tab}
                    <li class="is-active no-bottom">
                        <a>
                            <LocalizedText key="chatbot_manager.tab.{tab}" />
                        </a>
                    </li>
                {:else}
                    <li class="no-bottom">
                        <a on:click={() => (currentTab = tab)}>
                            <LocalizedText key="chatbot_manager.tab.{tab}" />
                        </a>
                    </li>
                {/if}
            {/each}
        </ul>
    </div>

    {#if currentTab == "COMMANDS"}
        <!-- <Commands> -->
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label class="checkbox">
            <LocalizedText key="chatbot_manager.commands.mention" slotMapping={["checkbox"]}>
                <input slot="0" type="checkbox" bind:checked={mentionSenderInReply} />
            </LocalizedText>
        </label>

        <br />
        <br />

        <ul>
            {#each commands as command}
                <li class="box">
                    <LocalizedText key="chatbot_manager.commands.format.{command.type}" slotMapping={["platform", "action", "action_target", "message"]}>
                        <div class="select" slot="0">
                            <select bind:value={command.platform}>
                                {#each PLATFORMS as platform}
                                    <option value={platform[0]}>{platform[1]}</option>
                                {/each}
                            </select>
                        </div>

                        <div class="select" slot="1">
                            <select bind:value={command.type}>
                                <option value="COMMAND">
                                    <LocalizedText key="chatbot_manager.commands.runs" />
                                </option>
                                <option value="CONTAINS">
                                    <LocalizedText key="chatbot_manager.commands.mentions" />
                                </option>
                            </select>
                        </div>

                        <span slot="2">
                            <input class="input" type="text" bind:value={command.trigger} style="width: 200px" />
                        </span>

                        <span slot="3">
                            <br />
                            <textarea class="textarea" bind:value={command.response} rows={2} />
                        </span>
                    </LocalizedText>

                    <a
                        class="item-delete has-text-danger"
                        on:click={() => {
                            commands.splice(commands.indexOf(command), 1);
                            commands = commands; // Trigger update.
                            updatePreferences();
                        }}
                    >
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
                            class="feather feather-trash"
                        >
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                    </a>
                </li>
            {/each}
            <li class="has-text-centered">
                <a
                    class="highlight-on-hover"
                    style="width: 100%; color: inherit; display: block; font-size: 1.5em;"
                    on:click={() => {
                        commands.push({
                            platform: null,
                            trigger: "casterlabs",
                            response: translate(App.get("language"), "chatbot_manager.example.command"),
                            type: "COMMAND"
                        });
                        commands = commands; // Trigger update.
                        updatePreferences();
                    }}
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="feather feather-plus"
                        style="width: 24px; height: 24px; margin-top: 10px;"
                    >
                        <line x1="12" y1="5" x2="12" y2="19" />
                        <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                </a>
            </li>
        </ul>
        <!-- </Commands> -->
    {:else if currentTab == "SHOUTS"}
        <!-- <Shouts> -->
        <ul>
            {#each shouts as shout}
                <li class="box">
                    <LocalizedText key="chatbot_manager.shouts.format" slotMapping={["platform", "action", "message"]}>
                        <div class="select" slot="0">
                            <select bind:value={shout.platform}>
                                {#each PLATFORMS as platform}
                                    <option value={platform[0]}>{platform[1]}</option>
                                {/each}
                            </select>
                        </div>

                        <div class="select" slot="1">
                            <select bind:value={shout.eventType}>
                                {#each SHOUT_EVENT_TYPES as eventType}
                                    <option value={eventType}>
                                        <LocalizedText key="chatbot_manager.shouts.{eventType}" />
                                    </option>
                                {/each}
                            </select>
                        </div>

                        <span slot="2">
                            <br />
                            <textarea class="textarea" bind:value={shout.text} rows={2} />
                        </span>
                    </LocalizedText>

                    <a
                        class="item-delete has-text-danger"
                        on:click={() => {
                            shouts.splice(shouts.indexOf(shout), 1);
                            shouts = shouts; // Trigger update.
                            updatePreferences();
                        }}
                    >
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
                            class="feather feather-trash"
                        >
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                    </a>
                </li>
            {/each}
            <li class="has-text-centered">
                <a
                    class="highlight-on-hover"
                    style="width: 100%; color: inherit; display: block; font-size: 1.5em;"
                    on:click={() => {
                        shouts.push({
                            platform: null,
                            eventType: "FOLLOW",
                            text: translate(App.get("language"), "chatbot_manager.example.shout")
                        });
                        shouts = shouts; // Trigger update.
                        updatePreferences();
                    }}
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="feather feather-plus"
                        style="width: 24px; height: 24px; margin-top: 10px;"
                    >
                        <line x1="12" y1="5" x2="12" y2="19" />
                        <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                </a>
            </li>
        </ul>
        <!-- </Shouts> -->
    {:else if currentTab == "TIMERS"}
        <!-- <Timers> -->

        <LocalizedText key="chatbot_manager.timers.format" slotMapping={["seconds"]}>
            <input slot="0" class="input" type="number" bind:value={timerInterval} style="width: 75px;" />
        </LocalizedText>
        <br />
        <ul>
            {#each timers as text}
                <li>
                    <textarea class="textarea" bind:value={text} rows={2} />

                    <a
                        class="item-delete has-text-danger"
                        on:click={() => {
                            timers.splice(timers.indexOf(text), 1);
                            timers = timers; // Trigger update.
                            updatePreferences();
                        }}
                    >
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
                            class="feather feather-trash"
                        >
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                    </a>
                </li>
            {/each}
            <li class="has-text-centered" style="margin-top: 5px;">
                <a
                    class="highlight-on-hover"
                    style="width: 100%; color: inherit; display: block; font-size: 1.5em;"
                    on:click={() => {
                        timers.push(translate(App.get("language"), "chatbot_manager.example.timer"));
                        timers = timers; // Trigger update.
                        updatePreferences();
                    }}
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="feather feather-plus"
                        style="width: 24px; height: 24px; margin-top: 10px;"
                    >
                        <line x1="12" y1="5" x2="12" y2="19" />
                        <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                </a>
            </li>
        </ul>
        <!-- </Timers> -->
    {:else if currentTab == "SETTINGS"}
        <!-- <Settings> -->

        <!-- ... TODO ... -->

        <!-- </Settings> -->
    {/if}
</div>

<style>
    .input {
        height: 32px !important;
        vertical-align: middle;
        display: inline-block;
        width: auto;
    }

    textarea {
        margin-top: 10px;
    }

    .select {
        vertical-align: middle;
        font-size: 13.25px !important;
    }

    li:not(.no-bottom) {
        font-size: 1em !important;
        position: relative;
        margin-bottom: 16px !important;
    }

    .item-delete {
        position: absolute;
        top: 6px;
        right: 5px;
    }

    .inline-children {
        display: inline-block;
        width: fit-content;
        font-weight: bold;
    }
</style>

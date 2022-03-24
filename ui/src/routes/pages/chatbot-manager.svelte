<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

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

    const SHOUT_EVENT_TYPES = [
        ["DONATION", "Donates"],
        ["FOLLOW", "Follows"],
        ["RAID", "Raids"],
        ["Subscription", "Subscribes"]
    ];

    let currentTab = "COMMANDS";

    let mentionSenderInReply = true;

    let commands = [
        {
            platform: null, // Null means all platforms
            trigger: "discord",
            response: "Join up @ discord.gg/example",
            type: "CONTAINS"
        },
        {
            platform: "CAFFEINE",
            trigger: "casterlabs",
            response: "Checkout casterlabs.co!",
            type: "COMMAND"
        }
    ];

    let shouts = [
        {
            platform: null, // Null means all platforms
            eventType: "FOLLOW",
            text: "Thank you for the follow!"
        }
    ];

    let timers = ["Checkout casterlabs.co for some sick widgets and alerts!"];

    let timerInterval = 90;

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - Chat Bot";
    });
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div style="margin: 15px;">
    <div class="tabs">
        <ul style="justify-content: center !important;">
            {#each ["COMMANDS", "SHOUTS", "TIMERS"] as tab}
                {#if currentTab == tab}
                    <li class="is-active">
                        <a>
                            {tab}
                        </a>
                    </li>
                {:else}
                    <li>
                        <a on:click={() => (currentTab = tab)}>
                            {tab}
                        </a>
                    </li>
                {/if}
            {/each}
        </ul>
    </div>

    {#if currentTab == "COMMANDS"}
        <!-- <Commands> -->
        <ul>
            {#each commands as command}
                <li class="box">
                    When someone from &nbsp;
                    <div class="select">
                        <select bind:value={command.platform}>
                            {#each PLATFORMS as platform}
                                <option value={platform[0]}>{platform[1]}</option>
                            {/each}
                        </select>
                    </div>
                    &nbsp;
                    <div class="select">
                        <select bind:value={command.type}>
                            <option value="COMMAND">runs</option>
                            <option value="CONTAINS">mentions</option>
                        </select>
                    </div>
                    &nbsp;
                    <span class="inline-children">
                        {#if command.type == "COMMAND"}
                            !
                        {:else}
                            &quot;
                        {/if}
                        <input class="input" type="text" bind:value={command.trigger} style="width: 200px" />
                        {#if command.type != "COMMAND"}
                            &quot;
                        {/if}
                        &nbsp;
                    </span>
                    {#if mentionSenderInReply}
                        reply with:
                    {:else}
                        say:
                    {/if}

                    <br />
                    <textarea class="textarea" bind:value={command.response} rows={2} />

                    <a
                        class="item-delete has-text-danger"
                        on:click={() => {
                            commands.splice(commands.indexOf(command), 1);
                            commands = commands; // Trigger update.
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
                            text: "Thank you for the follow!"
                        });
                        shouts = shouts; // Trigger update
                    }}
                >
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
                        class="feather feather-plus"
                        style="vertical-align: text-bottom;"
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
                    When someone from &nbsp;
                    <div class="select">
                        <select bind:value={shout.platform}>
                            {#each PLATFORMS as platform}
                                <option value={platform[0]}>{platform[1]}</option>
                            {/each}
                        </select>
                    </div>
                    &nbsp;
                    <div class="select">
                        <select bind:value={shout.eventType}>
                            {#each SHOUT_EVENT_TYPES as eventType}
                                <option value={eventType[0]}>{eventType[1]}</option>
                            {/each}
                        </select>
                    </div>
                    &nbsp; say:

                    <br />
                    <textarea class="textarea" bind:value={shout.text} rows={2} />

                    <a
                        class="item-delete has-text-danger"
                        on:click={() => {
                            shouts.splice(shouts.indexOf(shout), 1);
                            shouts = shouts; // Trigger update.
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
                        console.log("test", commands);
                        commands.push({
                            platform: null,
                            trigger: "casterlabs",
                            response: "Checkout casterlabs.co!",
                            type: "COMMAND"
                        });
                        commands = commands; // Trigger update
                    }}
                >
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
                        class="feather feather-plus"
                        style="vertical-align: text-bottom;"
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
        Every <input class="input" type="number" bind:value={timerInterval} style="width: 75px;" /> seconds, send one of the following:
        <br />
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
                        timers.push("Checkout casterlabs.co for some sick widgets and alerts!");
                        timers = timers; // Trigger update
                    }}
                >
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
                        class="feather feather-plus"
                        style="vertical-align: text-bottom;"
                    >
                        <line x1="12" y1="5" x2="12" y2="19" />
                        <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                </a>
            </li>
        </ul>
        <!-- </Timers> -->
    {/if}
</div>

<style>
    input {
        height: 32px !important;
        vertical-align: middle;
        display: inline-block;
        width: auto;
    }

    .select {
        vertical-align: middle;
        font-size: 13.25px !important;
    }

    li {
        font-size: 1em !important;
        position: relative;
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

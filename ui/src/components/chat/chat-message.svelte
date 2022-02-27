<svelte:options accessors />

<script>
    import User from "./user.svelte";

    const PLATFORMS_WITH_BAN = ["TWITCH", "TROVO"];
    const PLATFORMS_WITH_TIMEOUT = ["TWITCH", "TROVO"];
    const PLATFORMS_WITH_DELETE = ["TWITCH" /*, "BRIME" */, "TROVO"];
    const PLATFORMS_WITH_UPVOTE = ["CAFFEINE"];
    const PLATFORMS_WITH_RAID = ["CAFFEINE", "TWITCH", "TROVO"];

    export const timestamp = Date.now();

    export let koiEvent = null;
    export let isDeleted = false;
    export let upvotes = koiEvent.upvotes || 0;
    export let modAction = () => {};
    export let sender = null; // We set this.

    let isMessageFromSelf = false;
    let messageHtml = "";
    let highlight = false;
    let isPlatformMessage = koiEvent.event_type == "PLATFORM_MESSAGE";

    function escapeHtml(unsafe) {
        return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    let eventHasModIcons = false;

    if (koiEvent.event_type == "RAID") {
        const { host, viewers } = koiEvent;

        highlight = true;
        messageHtml = `${host.displayname} just raided with ${viewers} ${viewers == 1 ? "viewer" : "viewers"}`;
    } else if (koiEvent.event_type == "FOLLOW") {
        highlight = true;
        messageHtml = `${koiEvent.follower.displayname} just followed!`;
    } else if (koiEvent.event_type == "SUBSCRIPTION") {
        const { gift_recipient, subscriber, months } = koiEvent;

        switch (event.sub_type) {
            case "SUB":
                messageHtml = `${subscriber.displayname} just subscribed for ${months} ${months == 1 ? "month" : "months"}`;
                break;

            case "RESUB":
                messageHtml = `${subscriber.displayname} just resubscribed for ${months} ${months == 1 ? "month" : "months"}`;
                break;

            case "SUBGIFT":
                messageHtml = `${subscriber.displayname} just gifted ${gift_recipient.displayname} a ${months} month subscription`;
                break;

            case "RESUBGIFT":
                messageHtml = `${subscriber.displayname} just gifted ${gift_recipient.displayname} a ${months} month resubscription`;
                break;

            case "ANONSUBGIFT":
                messageHtml = `Anonymous just gifted ${gift_recipient.displayname} a ${months} month subscription`;
                break;

            case "ANONRESUBGIFT":
                messageHtml = `Anonymous just gifted ${gift_recipient.displayname} a ${months} month resubscription`;
                break;
        }

        highlight = true;
    } else if (koiEvent.event_type == "CHANNEL_POINTS") {
        const reward = koiEvent.reward;
        const imageHtml = `<img class="vcimage" src = "${reward.reward_image || reward.default_reward_image}" /> `;

        highlight = true;
        messageHtml = `${koiEvent.sender.displayname} just redeemed ${imageHtml}${reward.title}`;
    } else if (koiEvent.event_type == "CLEARCHAT") {
        highlight = true;
        messageHtml = `<i>Chat was cleared.</i>`;
    } else if (["CHAT", "DONATION", "PLATFORM_MESSAGE"].includes(koiEvent.event_type) /* Normal chat messages */) {
        messageHtml = escapeHtml(koiEvent.message);
        sender = koiEvent.sender;
        isMessageFromSelf = sender.UPID == koiEvent.streamer.UPID;

        for (const [name, image] of Object.entries(koiEvent.emotes)) {
            messageHtml = messageHtml.split(name).join(`<img class="inline-image" title="${name}" src="${image}" />`);
        }

        for (const pattern of koiEvent.links) {
            const link = pattern.includes("://") ? pattern : "https://" + pattern;

            messageHtml = messageHtml.split(pattern).join(`<a href="${link}" rel="external">${pattern}</a>`);
        }

        if (koiEvent.donations) {
            // Caffeine Props & Trovo Spells don't appear in chat like Twitch Cheers do.
            for (const donation of koiEvent.donations) {
                messageHtml += ` <img class="inline-image" src="${donation.image}" />`;
            }
        }

        if (koiEvent.event_type == "PLATFORM_MESSAGE") {
            eventHasModIcons = false;
            messageHtml = `<small class="platform-message"><i>${messageHtml}</i></small>`;
        } else {
            eventHasModIcons = true; // Enables the mod-icons.
        }
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<span class="chat-message {isDeleted ? 'is-deleted' : ''} {highlight ? 'highlighted' : ''} {isPlatformMessage ? 'no-select' : ''}">
    <span class="message-timestamp"> {new Date(timestamp).toLocaleTimeString()} </span>
    <span class="message-container">
        {#if eventHasModIcons}
            <span class="mod-actions">
                {#if PLATFORMS_WITH_BAN.includes(koiEvent.streamer.platform) && !isMessageFromSelf}
                    <a on:click={() => modAction("ban", koiEvent)} title="Ban">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="14"
                            height="14"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="var(--text-color)"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            class="feather feather-slash"
                        >
                            <circle cx="12" cy="12" r="10" />
                            <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
                        </svg>
                    </a>
                {/if}

                {#if PLATFORMS_WITH_TIMEOUT.includes(koiEvent.streamer.platform) && !isMessageFromSelf}
                    <a on:click={() => modAction("timeout", koiEvent)} title="Timeout">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="14"
                            height="14"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="var(--text-color)"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            class="feather feather-clock"
                        >
                            <circle cx="12" cy="12" r="10" />
                            <polyline points="12 6 12 12 16 14" />
                        </svg>
                    </a>
                {/if}

                {#if PLATFORMS_WITH_DELETE.includes(koiEvent.streamer.platform)}
                    <a on:click={() => modAction("delete", koiEvent)} title="Delete Message">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="14"
                            height="14"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="var(--text-color)"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            class="feather feather-trash-2"
                        >
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                            <line x1="10" y1="11" x2="10" y2="17" /><line x1="14" y1="11" x2="14" y2="17" />
                        </svg>
                    </a>
                {/if}
            </span>
        {/if}

        {#if sender}
            {#if isPlatformMessage}
                <!-- Only show the little icon if multiplatform, otherwise it's left out. -->
                <small class="platform-message-user">
                    <span class="platform-logo">
                        <img
                            src="/img/services/{koiEvent.sender.username.toLowerCase()}/icon.svg"
                            alt="{koiEvent.sender.displayname} Logo"
                            style={koiEvent.sender.username != "casterlabs" ? "filter: invert(var(--white-invert-factor));" : ""}
                        />
                    </span>
                    >
                </small>
            {:else}
                <!-- The &gt; is to help for copy pasting the chat. -->
                <!-- ItzLcyx test -->
                <!-- becomes: -->
                <!-- ItzLcyx > test -->
                <User userData={sender} /><span style="opacity: 0; font-size: 0;"> &gt; </span>
            {/if}
        {/if}

        <span>
            {@html messageHtml}
        </span>{#if upvotes > 0}
            <sup class="upvote-counter">
                {#if upvotes < 10}
                    <span class="upvote-1">{upvotes}</span>
                {:else if upvotes < 100}
                    <span class="upvote-2">{upvotes}</span>
                {:else if upvotes < 1000}
                    <span class="upvote-3">{upvotes}</span>
                {:else}
                    <span class="upvote-4">{upvotes}</span>
                {/if}
            </sup>
        {/if}
    </span>
</span>

<style>
    :global(.inline-image) {
        height: 1.3em;
        vertical-align: text-bottom;
    }

    .chat-message {
        padding-left: 10px;
        display: block;
        width: 100%;
        position: relative;
    }

    .message-timestamp {
        display: none;
    }

    :global(.show-timestamps) .message-container {
        margin-left: 3px;
    }

    :global(.show-timestamps) .message-timestamp {
        display: inline-block;
        font-weight: 625;
        font-size: 0.7em;
        transform: translateY(-2px) !important;
    }

    .highlighted {
        margin-top: 15px;
        margin-bottom: 15px;
        /* text-align: center; */
        background-color: rgba(0, 0, 0, 0.15);
    }

    .is-deleted {
        filter: grayscale(0.65);
        transition: 0.15s;
    }

    .is-deleted:hover {
        filter: grayscale(0);
        transition: 0.15s;
    }

    .upvote-counter {
        text-shadow: 1px 1px rgba(0, 0, 0, 0.75);
    }

    .mod-actions {
        display: none;
        user-select: none;
    }

    .mod-actions:last-child {
        padding-right: 5px;
    }

    :global(.enable-mod-actions) .mod-actions {
        display: inline;
    }

    .mod-actions svg {
        width: 15px;
        height: 15px;
    }

    /* Make the mod actions larger on mobile. */
    @media screen and (max-width: 768px) {
        .mod-actions svg {
            width: 18px;
            height: 18px;
            margin-right: 4px;
        }
    }

    :global(.platform-message) {
        font-weight: 500;
        font-size: 0.75em !important;
        line-height: 3em !important;
    }

    .platform-logo img {
        height: 16px;
        transform: translateY(2px);
    }

    /* Upvotes */
    :global(.upvote-1) {
        /* 1+ */
        color: #ff00ff;
    }

    :global(.upvote-2) {
        /* 10+ */
        color: #00ff00;
    }

    :global(.upvote-3) {
        /* 100+ */
        color: #ffff00;
    }

    :global(.upvote-4) {
        /* 1000+ */
        color: #ffffff;
    }
</style>

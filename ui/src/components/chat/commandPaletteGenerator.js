
// https://help.twitch.tv/s/article/chat-commands?language=en_US
const TWITCH_COMMANDS = {
    title: "Twitch",
    platform: "TWITCH",
    commands: [
        {
            command: "/ban",
            args: ["<username>", "[reason]"],
            description: "Permanently bans a user from chatting"
        },
        {
            command: "/unban",
            args: ["<username>"],
            description: "Removes a timeout or permenant ban on a user"
        },

        {
            command: "/timeout",
            args: ["<username>", "<duration>", "[reason]"],
            description: "Temporarily bans a user from chatting"
        },
        {
            command: "/untimeout",
            args: ["<username>"],
            description: "Removes a timeout ban on a user"
        },

        {
            command: "/clear",
            description: "Clears the chat history"
        },

        {
            command: "/emoteonly",
            description: "Only allows emotes in chat"
        },
        {
            command: "/emoteonlyoff",
            description: "Disable emote-only mode"
        },

        {
            command: "/followers",
            args: ["<duration>"],
            description: "Restricts chat to followers based on the duration they've been following"
        },
        {
            command: "/followersoff",
            description: "Disables followers-only mode"
        },

        {
            command: "/subscribers",
            description: "Restricts chat to subscribers only"
        },
        {
            command: "/subscribersoff",
            description: "Disables subscribers-only mode"
        },

        {
            command: "/host",
            args: ["<channel>"],
            description: "Host another stream on your channel"
        },
        {
            command: "/unhost",
            description: "Stops hosting the current hosted stream"
        },

        {
            command: "/marker",
            args: ["<description>"],
            description: "Adds a stream market at the current timestamp"
        },

        {
            command: "/mod",
            args: ["<username>"],
            description: "Grants moderator status to a user"
        },
        {
            command: "/unmod",
            args: ["<username>"],
            description: "Revokes a user's moderator status"
        },

        {
            command: "/vip",
            args: ["<username>"],
            description: "Grants VIP status to a user"
        },
        {
            command: "/unvip",
            args: ["<username>"],
            description: "Revokes a user's VIP status"
        },

        {
            command: "/raid",
            args: ["<channel>"],
            description: "Sends your viewers to another channel when your stream ends"
        },
        {
            command: "/unraid",
            description: "Cancels the ongoing raid"
        },

        {
            command: "/restrict",
            args: ["<username>"],
            description: "Starts restricting a user's messages"
        },
        {
            command: "/restrict",
            args: ["<username>"],
            description: "Stops restricting a user's messages"
        },

        {
            command: "/slow",
            args: ["<duration>"],
            description: "Limit how frequently users can send messages in chat"
        },
        {
            command: "/slowoff",
            description: "Disables slow mode"
        },

        {
            command: "/uniquechat",
            description: "Prevents users from sending duplicate messages in chat"
        },
        {
            command: "/uniquechatoff",
            description: "Disables unique-chats only mode"
        }
    ]
};

// https://trovo.live/support?lang=en&topicid=CA08264516974D9F%2FE3D657FFFFDE0929
// Also see the command popup in chat.
const TROVO_COMMANDS = {
    title: "Trovo",
    platform: "TROVO",
    commands: [
        {
            command: "/mods",
            description: "Shows the list of moderators"
        },

        {
            command: "/banned",
            description: "Shows the list of banned chatters"
        },

        {
            command: "/ban",
            args: ["<username>", "[duration]"],
            description: "Permanently bans a user from chatting, with an optional duration"
        },
        {
            command: "/unban",
            args: ["<username>"],
            description: "Removes a timeout or permenant ban on a user"
        },

        {
            command: "/host",
            args: ["<channel>"],
            description: "Stops the stream and start's hosting the specified channel"
        },
        {
            command: "/unhost",
            description: "Stops hosting the current hosted stream"
        },

        {
            command: "/bulletscreenon",
            description: "Allows viewers to send bullet screen chats"
        },
        {
            command: "/bulletscreenoff",
            description: "Prevents viewers to send bullet screen chats"
        },

        {
            command: "/mod",
            args: ["<username>"],
            description: "Grants moderator status to a user"
        },
        {
            command: "/unmod",
            args: ["<username>"],
            description: "Revokes a user's moderator status"
        },

        {
            command: "/clear",
            description: "Clears the chat history"
        },

        {
            command: "/followers",
            args: ["<duration>"],
            description: "Restricts chat to followers based on the duration they've been following"
        },
        {
            command: "/followersoff",
            description: "Disables followers-only mode"
        },

        {
            command: "/slow",
            args: ["<duration>"],
            description: "Limit how frequently users can send messages in chat"
        },
        {
            command: "/slowoff",
            description: "Disables slow mode"
        },

    ]
};

const CAFFEINE_COMMANDS = {
    title: "Caffeine",
    platform: "CAFFEINE",
    commands: [
        {
            command: "/afterparty",
            args: ["<channel>"],
            description: "Starts an afterparty and sends your viewers to the speficied channel"
        }
    ]
};

export function generate(signedInPlatforms) {
    let commandSections = [];

    // TODO Caffeinated Commands

    if (signedInPlatforms.includes("TWITCH")) {
        commandSections.push(TWITCH_COMMANDS);
    }

    if (signedInPlatforms.includes("TROVO")) {
        commandSections.push(TROVO_COMMANDS);
    }

    if (signedInPlatforms.includes("CAFFEINE")) {
        commandSections.push(CAFFEINE_COMMANDS);
    }

    return commandSections;
}

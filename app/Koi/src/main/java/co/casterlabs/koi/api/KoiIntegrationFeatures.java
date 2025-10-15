package co.casterlabs.koi.api;

public enum KoiIntegrationFeatures {

    /* ---- Misc ---- */

    /**
     * @deprecated Use {@link #STREAM_INFO}
     */
    UPDATE_STREAM_INFO,
    STREAM_INFO,
    PUBLISHING_INFO,
    UPDATE_ROOM_STATE,
    CHAT_BOT_LINKING,
    CHANNEL_POINTS,
    HYPE_TRAIN,
    STREAM_KEY,
    ADVERTISEMENTS,

    /* ---- Alerts / Counts / Events ---- */

    DONATION_ALERT,
    FOLLOWER_ALERT,
    SUBSCRIPTION_ALERT,
    RAID_ALERT,

    FOLLOWER_COUNT,
    SUBSCRIBER_COUNT,

    CHAT,
    STREAM_STATUS,
    ROOMSTATE,

    /* ---- Viewers ---- */

    VIEWERS_LIST,
    VIEWERS_COUNT,
    VIEWERS_PRESENCE,

    /* ---- Chat & Moderation ---- */

    MESSAGE_UPVOTE,
    MESSAGE_REACTION,
    MESSAGE_DELETION,

    CHAT_SEND_MESSAGE,
    CHAT_SEND_COMMAND,

    ;

}

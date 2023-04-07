package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@AllArgsConstructor
@SuppressWarnings("deprecation")
public enum KoiEventType {
    // @formatter:off
    FOLLOW               (FollowEvent.class),
    SUBSCRIPTION         (SubscriptionEvent.class),
    USER_UPDATE          (UserUpdateEvent.class),
    STREAM_STATUS        (StreamStatusEvent.class),
    META                 (MessageMetaEvent.class),
    VIEWER_JOIN          (ViewerJoinEvent.class),
    VIEWER_LEAVE         (ViewerLeaveEvent.class),
    VIEWER_LIST          (ViewerListEvent.class),
    RAID                 (RaidEvent.class),
    CHANNEL_POINTS       (ChannelPointsEvent.class),
    CATCHUP              (CatchupEvent.class),
    CLEARCHAT            (ClearChatEvent.class),
    ROOMSTATE            (RoomstateEvent.class),
    PLATFORM_MESSAGE     (PlatformMessageEvent.class), 
    RICH_MESSAGE         (RichMessageEvent.class),
    MESSAGE_REACTION     (MessageReactionEvent.class),
    
    @Deprecated
    DONATION             (DonationEvent.class),
    @Deprecated
    CHAT                 (ChatEvent.class),
    ;
    // @formatter:on

    private Class<? extends KoiEvent> eventClass;

    public static KoiEvent get(JsonObject eventJson) {
        String eventType = eventJson.getString("event_type");

        try {
            // 1) Lookup the event type
            // 2) Use RSON to deserialize to object using the eventClass.
            // 3) Profit!
            KoiEventType type = KoiEventType.valueOf(eventType);
            KoiEvent event = Rson.DEFAULT.fromJson(eventJson, type.eventClass);

            return event;
        } catch (IllegalArgumentException e) {
            // 1.1) Lookup failed, so we don't actually have that event.
            // 1.2) Return nothing.
            return null;
        } catch (Exception e) {
            // 2.1) *Something* failed, so we probably don't have that event structured
            // correctly.
            // 2.2) Return nothing.
            FastLogger.logStatic(LogLevel.SEVERE, "An error occured while converting an event of type %s", eventType);
            FastLogger.logException(e);
            FastLogger.logStatic(LogLevel.DEBUG, eventJson);
            return null;
        }
    }

}

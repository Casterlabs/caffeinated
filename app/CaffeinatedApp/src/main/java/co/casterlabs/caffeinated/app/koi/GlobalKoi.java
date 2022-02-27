package co.casterlabs.caffeinated.app.koi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AuthInstance;
import co.casterlabs.caffeinated.app.koi.events.AppKoiChatDeleteEvent;
import co.casterlabs.caffeinated.app.koi.events.AppKoiChatSendEvent;
import co.casterlabs.caffeinated.app.koi.events.AppKoiChatUpvoteEvent;
import co.casterlabs.caffeinated.app.koi.events.AppKoiEventType;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.BridgeValue;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.events.CatchupEvent;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.RoomstateEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
public class GlobalKoi extends Koi implements KoiLifeCycleHandler {
    private static final List<KoiEventType> KEPT_EVENTS = Arrays.asList(
        KoiEventType.FOLLOW,
        KoiEventType.CHAT,
        KoiEventType.DONATION,
        KoiEventType.SUBSCRIPTION,
        KoiEventType.META,
        KoiEventType.VIEWER_JOIN,
        KoiEventType.VIEWER_LEAVE,
        KoiEventType.RAID,
        KoiEventType.CHANNEL_POINTS,
        KoiEventType.CLEARCHAT,
        KoiEventType.PLATFORM_MESSAGE
    );

    private static EventHandler<AppKoiEventType> handler = new EventHandler<>();

    private List<KoiLifeCycleHandler> koiEventListeners = new LinkedList<>();

    private List<KoiEvent> eventHistory = new LinkedList<>();
    private Map<UserPlatform, List<User>> viewers = new ConcurrentHashMap<>();
    private Map<UserPlatform, UserUpdateEvent> userStates = new ConcurrentHashMap<>();
    private Map<UserPlatform, StreamStatusEvent> streamStates = new ConcurrentHashMap<>();
    private Map<UserPlatform, RoomstateEvent> roomStates = new ConcurrentHashMap<>();

    private BridgeValue<Map<UserPlatform, List<User>>> viewersBridge = new BridgeValue<>("koi:viewers", Collections.unmodifiableMap(this.viewers));
    private BridgeValue<Map<UserPlatform, UserUpdateEvent>> userStatesBridge = new BridgeValue<>("koi:userStates", Collections.unmodifiableMap(this.userStates));
    private BridgeValue<Map<UserPlatform, StreamStatusEvent>> streamStatesBridge = new BridgeValue<>("koi:streamStates", Collections.unmodifiableMap(this.streamStates));
    private BridgeValue<Map<UserPlatform, RoomstateEvent>> roomStatesBridge = new BridgeValue<>("koi:roomStates", Collections.unmodifiableMap(this.roomStates));

    public void init() {
        handler.register(this);

        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.viewersBridge);
        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.userStatesBridge);
        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.streamStatesBridge);
        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.roomStatesBridge);

        // We don't use it, so it'll just sit here in purgatory.
        CaffeinatedApp.getInstance().getAppBridge().attachValue(
            new BridgeValue<>("koi:history", Collections.unmodifiableList(this.eventHistory), false)
        );
    }

    /**
     * @deprecated Should <b>only</b> be called from AppAuth.
     */
    @Deprecated
    public void updateFromAuth() {
        List<UserPlatform> validPlatforms = new LinkedList<>();

        for (AuthInstance inst : CaffeinatedApp.getInstance().getAuth().getAuthInstances().values()) {
            if (inst.getUserData() != null) {
                validPlatforms.add(inst.getUserData().getPlatform());
            }
        }

        for (UserPlatform key : new ArrayList<>(this.viewers.keySet())) {
            if (!validPlatforms.contains(key)) {
                this.viewers.remove(key);
                this.userStates.remove(key);
                this.streamStates.remove(key);
            }
        }

        this.updateBridgeData();
    }

    private void updateBridgeData() {
        // History has updates disabled.
        this.viewersBridge.update();
        this.userStatesBridge.update();
        this.streamStatesBridge.update();
        this.roomStatesBridge.update();

        // Send update to the widget instances.
        new AsyncTask(() -> {
            JsonObject statistics = new JsonObject()
                .put("history", Rson.DEFAULT.toJson(this.eventHistory))
                .put("viewers", Rson.DEFAULT.toJson(this.viewers))
                .put("userStates", Rson.DEFAULT.toJson(this.userStates))
                .put("streamStates", Rson.DEFAULT.toJson(this.streamStates))
                .put("roomStates", Rson.DEFAULT.toJson(this.roomStates));

            for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
                for (Widget widget : plugin.getWidgets()) {
                    for (WidgetInstance instance : widget.getWidgetInstances()) {
                        try {
                            instance.onKoiStaticsUpdate(statistics);
                        } catch (IOException ignored) {}
                    }
                }
            }
        });
    }

    @KoiEventHandler
    public void onEvent(KoiEvent e) {
        if (e.getType() == KoiEventType.CATCHUP) {
            CatchupEvent catchUp = (CatchupEvent) e;

            // Loop through the catchup events,
            // Convert them to an event,
            // Check to make sure that conversion succeeded,
            // Broadcast that event.
            // (We need to do these in order)
            for (JsonElement element : catchUp.getEvents()) {
                KoiEvent cEvent = KoiEventType.get(element.getAsObject());

                if (cEvent != null) {
                    this.onEvent(cEvent);
                }
            }
        } else {
            if (KEPT_EVENTS.contains(e.getType())) {
                this.eventHistory.add(e);
            }

            switch (e.getType()) {

                case VIEWER_LIST: {
                    this.viewers.put(
                        e.getStreamer().getPlatform(),
                        Collections.unmodifiableList(((ViewerListEvent) e).getViewers())
                    );

                    this.updateBridgeData();
                    break;
                }

                case USER_UPDATE: {
                    this.userStates.put(
                        e.getStreamer().getPlatform(),
                        (UserUpdateEvent) e
                    );

                    this.updateBridgeData();
                    break;
                }

                case STREAM_STATUS: {
                    this.streamStates.put(
                        e.getStreamer().getPlatform(),
                        (StreamStatusEvent) e
                    );

                    this.updateBridgeData();
                    break;
                }

                case ROOMSTATE: {
                    this.roomStates.put(
                        e.getStreamer().getPlatform(),
                        (RoomstateEvent) e
                    );

                    this.updateBridgeData();
                    break;
                }

                default:
                    break;
            }

            // Emit the event to Caffeinated.
            JsonElement asJson = Rson.DEFAULT.toJson(e);

            CaffeinatedApp.getInstance().getAppBridge().emit("koi:event:" + e.getType().name().toLowerCase(), asJson);
            CaffeinatedApp.getInstance().getAppBridge().emit("koi:event", asJson);

            // These are used internally.
            for (KoiLifeCycleHandler listener : this.koiEventListeners) {
                KoiEventUtil.reflectInvoke(listener, e);
            }

            // Notify the plugins
            for (CaffeinatedPlugin pl : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
                pl.fireKoiEventListeners(e);
            }
        }

        FastLogger.logStatic(
            LogLevel.DEBUG,
            "Processed %s event for %s.",
            e.getType().name().toLowerCase().replace('_', ' '),
            e.getStreamer().getDisplayname()
        );
    }

    @EventListener
    public void onKoiChatSendEvent(AppKoiChatSendEvent event) {
        if (event.getMessage().startsWith("/koi test ")) {
            this.sendTest(
                event.getPlatform(),
                event.getMessage().substring("/koi test ".length())
            );
        } else {
            this.sendChat(event.getPlatform(), event.getMessage(), KoiChatterType.CLIENT);
        }
    }

    @EventListener
    public void onKoiChatUpvoteEvent(AppKoiChatUpvoteEvent event) {
        this.upvoteChat(event.getPlatform(), event.getMessageId());
    }

    @EventListener
    public void onKoiChatDeleteEvent(AppKoiChatDeleteEvent event) {
        this.deleteChat(event.getPlatform(), event.getMessageId());
    }

    @Override
    public void sendChat(@NonNull UserPlatform platform, @NonNull String message, @NonNull KoiChatterType chatter) {
        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.sendChat(message, chatter);
        }
    }

    public void sendTest(@NonNull UserPlatform platform, @NonNull String eventType) {
        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.sendTest(eventType);
        }
    }

    @Override
    public void upvoteChat(@NonNull UserPlatform platform, @NonNull String messageId) {
        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.upvoteChat(messageId);
        }
    }

    @Override
    public void deleteChat(@NonNull UserPlatform platform, @NonNull String messageId) {
        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.deleteChat(messageId);
        }
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppKoiEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

    @Override
    public List<KoiEvent> getEventHistory() {
        return Collections.unmodifiableList(this.eventHistory);
    }

    @Override
    public Map<UserPlatform, List<User>> getViewers() {
        return Collections.unmodifiableMap(this.viewers);
    }

    @Override
    public Map<UserPlatform, UserUpdateEvent> getUserStates() {
        return Collections.unmodifiableMap(this.userStates);
    }

    @Override
    public Map<UserPlatform, StreamStatusEvent> getStreamStates() {
        return Collections.unmodifiableMap(this.streamStates);
    }

    @Override
    public Map<UserPlatform, RoomstateEvent> getRoomStates() {
        return Collections.unmodifiableMap(this.roomStates);
    }

}

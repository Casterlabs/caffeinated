package co.casterlabs.caffeinated.localserver.websocket;

import java.io.IOException;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rhs.session.Websocket;
import co.casterlabs.rhs.session.WebsocketListener;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class RealtimeWidgetListener implements WebsocketListener, RouteHelper {
    private WidgetHandle handle;
    private WidgetInstanceMode mode;
    private String connectionId;

    private WidgetInstanceProvider wInstance;
    private RealtimeConnection connInstance;
    private Websocket websocket;

    @SneakyThrows
    public RealtimeWidgetListener(Widget widget, WidgetInstanceMode mode, String connectionId) {
        this.handle = ReflectionLib.getValue(widget, "$handle");
        this.mode = mode;
        this.connectionId = connectionId;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onOpen(Websocket websocket) {
        this.websocket = websocket;

        this.connInstance = new ConnectionWrapper();
        this.wInstance = new WidgetInstanceProvider();

        Pair<RealtimeConnection, WidgetInstance> connPair = new Pair<>(this.connInstance, this.wInstance);
        websocket.setAttachment(connPair);

        JsonObject statics = null;

        switch (this.mode) {
            case APPLET:
            case DOCK:
            case SETTINGS_APPLET:
                statics = Caffeinated.getInstance().getKoi().toJsonExtended();
                break;

            case DEMO:
            case WIDGET:
            case WIDGET_ALT:
                statics = Caffeinated.getInstance().getKoi().toJson();
                break;
        }

        this.sendMessage(
            "KOI_STATICS",
            statics
        );

        this.sendMessage(
            "MUSIC",
            Caffeinated.getInstance().getMusic().toJson()
        );

        this.sendMessage(
            "APP",
            CaffeinatedApp.getInstance().getUI().constructSDKPreferences()
        );

        this.sendMessage(
            "INIT",
            new JsonObject()
                .put("connectionId", this.connectionId)
                .put("widget", Rson.DEFAULT.toJson(this.handle))
                .put("koi", Caffeinated.getInstance().getKoi().toJson())
                .put("basePath", this.handle.widget.getWidgetBasePath(this.mode))
        );
    }

    @Override
    public void onText(Websocket websocket, String raw) {
        try {
            JsonObject message = Rson.DEFAULT.fromJson(raw, JsonObject.class);

            String type = message.getString("type").toUpperCase();

            switch (type) {

                case "READY": {
                    this.handle.widgetInstances.add(this.wInstance);
                    this.handle.widget.onNewInstance(this.wInstance);
                    return;
                }

                case "KOI": {
                    JsonObject data = message.getObject("data");
                    FastLogger.logStatic(data);

                    String koiType = data.getString("type");
                    UserPlatform platform = null;
                    if (data.get("platform").isJsonString()) {
                        platform = UserPlatform.valueOf(data.getString("platform"));
                    }

                    switch (koiType) {
                        case "UPVOTE": {
                            String messageId = data.getString("messageId");
                            Caffeinated.getInstance().getKoi().upvoteChat(platform, messageId);
                            return;
                        }

                        case "DELETE": {
                            String messageId = data.getString("messageId");
                            boolean isUserGesture = data.getBoolean("isUserGesture");
                            Caffeinated.getInstance().getKoi().deleteChat(platform, messageId, isUserGesture);
                            return;
                        }

                        case "MESSAGE": {
                            KoiChatterType chatter = KoiChatterType.valueOf(data.getString("chatter"));
                            String replyTarget = data.get("replyTarget").isJsonNull() ? null : data.getString("replyTarget");
                            boolean isUserGesture = data.getBoolean("isUserGesture");
                            String text = data.getString("message");

                            Caffeinated.getInstance().getKoi().sendChat(
                                platform,
                                text,
                                chatter,
                                replyTarget,
                                isUserGesture
                            );
                            return;
                        }

                        default:
                            return;
                    }
                }

                case "OPEN_LINK": {
                    JsonObject data = message.getObject("data");
                    String link = data.getString("link");
                    CaffeinatedApp.getInstance().openLink(link);
                    return;
                }

                case "EMISSION": {
                    JsonObject data = message.getObject("data");
                    String emissionType = data.getString("type");
                    JsonElement emissionPayload = data.get("data");

                    this.wInstance.broadcast(emissionType, emissionPayload);
                    return;
                }

                case "LOCALIZE": {
                    JsonObject data = message.getObject("data");
                    String nonce = data.getString("nonce");

                    if (!data.containsKey("key")) {
                        this.sendMessage(
                            "LOCALIZE",
                            new JsonObject()
                                .put("nonce", nonce)
                                .put("value", "")
                        );
                        return;
                    }

                    try {
                        String key = data.getString("key");
                        JsonObject knownPlaceholders = data.getObject("knownPlaceholders");
                        JsonArray knownComponents = data.getArray("knownComponents");

                        String value = CaffeinatedApp.getInstance().localize(key, knownPlaceholders, knownComponents);

                        this.sendMessage(
                            "LOCALIZE",
                            new JsonObject()
                                .put("nonce", nonce)
                                .put("value", value)
                        );
                    } catch (Throwable t) {
                        this.sendMessage(
                            "LOCALIZE",
                            new JsonObject()
                                .put("nonce", nonce)
                                .put("value", "INTERNAL_ERROR")
                        );
                        throw t;
                    }
                    return;
                }

                case "PONG": {
                    this.connInstance.resetTimeout();
                    return;
                }

            }
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
    }

    @Override
    public void onClose(Websocket websocket) {
        try {
            this.wInstance.onClose();
        } finally {
            this.handle.widgetInstances.remove(this.wInstance);
        }
    }

    private void sendMessage(String type, JsonObject payload) {
        String json = new JsonObject()
            .put("type", type.toUpperCase())
            .put("data", payload)
            .toString();

//        FastLogger.logStatic(LogLevel.TRACE, json);

        try {
            this.websocket.send(
                json
            );
        } catch (Throwable ignored) {}
    }

    private class ConnectionWrapper extends RealtimeConnection {

        @Override
        protected void ping() {
            sendMessage("PING", new JsonObject());
        }

        @Override
        public void close() throws IOException {
            websocket.close();
        }

    }

    private class WidgetInstanceProvider extends WidgetInstance {

        public WidgetInstanceProvider() {
            super(mode, connectionId, handle.widget);
        }

        /* ---------------- */
        /* Expose these     */
        /* ---------------- */

        @Deprecated
        @Override
        public void broadcast(@NonNull String type, @NonNull JsonElement message) {
            super.broadcast(type, message);
        }

        @Deprecated
        @Override
        public void onClose() {
            super.onClose();
        }

        /* ---------------- */

        @Override
        protected void emit0(@NonNull String type, @NonNull JsonElement message) throws IOException {
            sendMessage(
                "EMISSION",
                new JsonObject()
                    .put("type", type)
                    .put("data", message)
            );
        }

        @Override
        public @NonNull String getRemoteIpAddress() {
            return websocket.getSession().getRemoteIpAddress();
        }

        @Override
        public void close() throws IOException {
            websocket.close();
        }

        @Override
        public void onKoiEvent(@NonNull KoiEvent event) throws IOException {
            sendMessage("KOI", Rson.DEFAULT.toJson(event).getAsObject());
        }

        @Override
        public void onSettingsUpdate() {
            sendMessage(
                "UPDATE",
                new JsonObject()
                    .put("widget", Rson.DEFAULT.toJson(handle))
            );
        }

        @Override
        public void onKoiStaticsUpdate(@NonNull JsonObject statics) throws IOException {
            sendMessage(
                "KOI_STATICS",
                statics
            );
        }

        @Override
        public void onMusicUpdate(@NonNull JsonObject music) throws IOException {
            sendMessage(
                "MUSIC",
                music
            );
        }

        @Override
        public void onAppearanceUpdate(@NonNull JsonObject preferences) throws IOException {
            sendMessage(
                "APPEARANCE",
                preferences
            );
        }

    }

}

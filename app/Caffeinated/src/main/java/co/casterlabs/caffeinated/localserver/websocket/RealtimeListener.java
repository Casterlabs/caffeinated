package co.casterlabs.caffeinated.localserver.websocket;

import java.io.IOException;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.RealtimeApiListener;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rhs.session.Websocket;
import co.casterlabs.rhs.session.WebsocketListener;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class RealtimeListener implements WebsocketListener, RouteHelper {
    private String connectionId;

    private AppListener appListener;
    private RealtimeConnection connInstance;
    private Websocket websocket;

    @SneakyThrows
    public RealtimeListener(String connectionId) {
        this.connectionId = connectionId;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onOpen(Websocket websocket) {
        this.websocket = websocket;

        this.appListener = new AppListener();
        this.connInstance = new ConnectionWrapper();

        Pair<RealtimeConnection, AppListener> connPair = new Pair<>(this.connInstance, this.appListener);
        websocket.setAttachment(connPair);

        JsonObject statics = Caffeinated.getInstance().getKoi().toJsonExtended();

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
                .putNull("widget")
                .put("koi", Caffeinated.getInstance().getKoi().toJson())
                .putNull("basePath")
        );
    }

    @Override
    public void onText(Websocket websocket, String raw) {
        try {
            JsonObject message = Rson.DEFAULT.fromJson(raw, JsonObject.class);

            String type = message.getString("type").toUpperCase();

            switch (type) {

                case "READY": {
                    CaffeinatedApp.getInstance().getApiListeners().add(this.appListener);
                    return;
                }

                case "KOI": {
                    JsonObject data = message.getObject("data");
                    FastLogger.logStatic(data);

                    String koiType = data.getString("type");
                    UserPlatform platform = UserPlatform.valueOf(data.getString("platform"));

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

                case "EMISSION":
                    // Ignored.
                    return;

                case "LOCALIZE": {
                    JsonObject data = message.getObject("data");
                    String nonce = data.getString("nonce");

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
        CaffeinatedApp.getInstance().getApiListeners().remove(this.appListener);
    }

    @SneakyThrows
    private void sendMessage(String type, JsonObject payload) {
        String json = new JsonObject()
            .put("type", type.toUpperCase())
            .put("data", payload)
            .toString();

//        FastLogger.logStatic(LogLevel.TRACE, json);

        this.websocket.send(
            json
        );
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

    private class AppListener implements RealtimeApiListener {

        @Override
        public void onKoiEvent(@NonNull KoiEvent event) throws IOException {
            sendMessage("KOI", Rson.DEFAULT.toJson(event).getAsObject());
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

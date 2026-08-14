package co.casterlabs.caffeinated.localserver.websocket;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.app.AppEventBus;
import co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.localserver.LocalServer;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.util.EventBus;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rhs.protocol.websocket.Websocket;
import co.casterlabs.rhs.protocol.websocket.WebsocketListener;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class RealtimeListener implements WebsocketListener {
    private String connectionId;

    private List<EventBus<?, ?>.Subscription> eventBusSubscriptions = new LinkedList<>();
    private RealtimeConnection connInstance;
    private Websocket websocket;

    @SneakyThrows
    public RealtimeListener(String connectionId) {
        this.connectionId = connectionId;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onOpen(Websocket websocket) {
        LocalServer.websockets.add(websocket);
        this.websocket = websocket;

        this.connInstance = new ConnectionWrapper();

        Pair<RealtimeConnection, Object> connPair = new Pair<>(this.connInstance, null);
        websocket.attachment(connPair);

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
            AppUI.constructSDKPreferences()
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
                    this.eventBusSubscriptions.add(
                        AppEventBus.bus.subscribe(
                            AppEventBus.KOI_EVENT, (KoiEvent event) -> {
                                sendMessage("KOI", Rson.DEFAULT.toJson(event).getAsObject());
                            }
                        )
                    );
                    this.eventBusSubscriptions.add(
                        AppEventBus.bus.subscribe(
                            AppEventBus.KOI_STATICS, (JsonObject statics) -> {
                                sendMessage("KOI_STATICS", statics);
                            }
                        )
                    );
                    this.eventBusSubscriptions.add(
                        AppEventBus.bus.subscribe(
                            AppEventBus.MUSIC_UPDATE, (JsonObject music) -> {
                                sendMessage("MUSIC", music);
                            }
                        )
                    );
                    this.eventBusSubscriptions.add(
                        AppEventBus.bus.subscribe(
                            AppEventBus.APPEARANCE_UPDATE, (JsonObject preferences) -> {
                                sendMessage("APPEARANCE", preferences);
                            }
                        )
                    );
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
                    Caffeinated.getInstance().openLink(link);
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
                        JsonObject knownComponents = data.getObject("knownComponents");

                        String value = CaffeinatedImpl.INSTANCE.localize(
                            key,
                            Rson.DEFAULT.fromJson(knownPlaceholders, new TypeToken<Map<String, String>>() {
                            }),
                            Rson.DEFAULT.fromJson(knownComponents, new TypeToken<Map<String, String>>() {
                            })
                        );

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
        LocalServer.websockets.remove(websocket);
        for (EventBus<?, ?>.Subscription subscription : this.eventBusSubscriptions) {
            subscription.revoke();
        }
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

}

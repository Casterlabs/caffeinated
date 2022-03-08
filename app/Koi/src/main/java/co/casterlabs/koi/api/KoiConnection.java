package co.casterlabs.koi.api;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.stream.KoiStreamConfiguration;
import co.casterlabs.koi.api.stream.KoiStreamConfigurationFeatures;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KoiConnection implements Closeable {
    public static final String KOI_URL = "wss://api.casterlabs.co/v2/koi";

    private KoiLifeCycleHandler listener;
    private FastLogger logger;
    private KoiSocket socket;

    private URI uri;

    private JsonObject request;

    @SneakyThrows
    public KoiConnection(@NonNull String url, @NonNull FastLogger logger, @NonNull KoiLifeCycleHandler listener, String clientId) {
        this.logger = logger;
        this.listener = listener;
        this.uri = new URI(url + "?client_id=" + clientId);
    }

    @Override
    public void close() {
        this.socket.close();
    }

    public boolean isConnected() {
        if (this.socket == null) {
            return false;
        } else {
            return this.socket.isOpen();
        }
    }

    public KoiConnection hookStreamStatus(String username, UserPlatform platform) throws InterruptedException {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected.");
        } else {
            this.request = new JsonObject();

            this.request.put("type", "USER_STREAM_STATUS");
            this.request.put("username", username);
            this.request.put("platform", platform.name());
            this.request.put("nonce", "_login");

            this.socket = new KoiSocket(this.uri);
            this.socket.connectBlocking();

            return this;
        }
    }

    public KoiConnection login(String token) throws InterruptedException {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected.");
        } else {
            this.request = new JsonObject();

            this.request.put("type", "LOGIN");
            this.request.put("token", token);
            this.request.put("nonce", "_login");

            this.socket = new KoiSocket(this.uri);
            this.socket.connectBlocking();

            return this;
        }
    }

    private class KoiSocket extends WebSocketClient {

        public KoiSocket(URI uri) {
            super(uri);

            this.setConnectionLostTimeout(5 /* Seconds */);
            this.addHeader("User-Agent", "Casterlabs");
            this.setTcpNoDelay(true);
        }

        @Override
        public boolean connectBlocking() throws InterruptedException {
            logger.info("Connecting to Koi...");
            return super.connectBlocking();
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            logger.info("Connected to Koi.");

            if (request == null) {
                this.close();
            } else {
                this.send(request.toString());
                request = null;
            }
        }

        @Override
        public void send(String text) {
            logger.debug("\u2191 " + text);

            super.send(text);
        }

        private void keepAlive(JsonElement nonce) {
            JsonObject request = new JsonObject();

            request.put("type", "KEEP_ALIVE");
            request.put("nonce", nonce);

            this.send(request.toString());
        }

        @Override
        public void onMessage(String raw) {
            logger.debug("\u2193 " + raw);

            try {
                JsonObject packet = Rson.DEFAULT.fromJson(raw, JsonObject.class);

                switch (packet.getString("type")) {
                    case "KEEP_ALIVE": {
                        this.keepAlive(packet.get("nonce"));
                        return;
                    }

                    case "FEATURES": {
                        listener.onSupportedFeatures(
                            Rson.DEFAULT.fromJson(
                                packet.get("features"),
                                new TypeToken<List<KoiIntegrationFeatures>>() {
                                }
                            )
                        );
                        return;
                    }

                    case "PLATFORM_CATEGORIES_LIST": {
                        Map<String, String> categories = new HashMap<>();

                        for (Entry<String, JsonElement> entry : packet.getObject("categories").entrySet()) {
                            categories.put(entry.getKey(), entry.getValue().getAsString());
                        }

                        listener.onPlatformCategories(categories);
                        return;
                    }

                    case "STREAM_CONFIGURATION_FEATURES": {
                        listener.onSupportedStreamConfigurationFeatures(
                            Rson.DEFAULT.fromJson(
                                packet.get("supported"),
                                new TypeToken<List<KoiStreamConfigurationFeatures>>() {
                                }
                            )
                        );
                        return;
                    }

                    case "SERVER": {
                        listener.onServerMessage(packet.get("server").getAsString());
                        return;
                    }

                    case "ERROR": {
                        listener.onError(packet.get("error").getAsString());
                        return;
                    }

                    case "EVENT": {
                        JsonObject eventJson = packet.getObject("event");
                        KoiEvent event = KoiEventType.get(eventJson);

                        if (event == null) {
                            logger.warn("Unsupported event type: %s", eventJson.getString("event_type"));
                        } else {
                            KoiEventUtil.reflectInvoke(listener, event);
                        }
                        return;
                    }

                    // We don't care about these or we already have them.
                    case "WELCOME":
                    case "CLIENT_SCOPES":
                    case "CONTENT_RATINGS":
                    case "LANGUAGES":
                        return;

                    default:
                        FastLogger.logStatic(LogLevel.DEBUG, "Unknown message type: %s\n%s", packet.getString("type"), packet);
                        return;
                }
            } catch (Exception e) {
                FastLogger.logStatic(LogLevel.SEVERE, raw);
                listener.onException(e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (remote) {
                logger.info("Lost connection to Koi.");
            } else {
                logger.info("Disconnected from Koi.");
            }

            this.fireCloseEvent(remote);
        }

        @Override
        public void onError(Exception e) {
            if (e instanceof IOException) {
                logger.info("Connection to Koi failed.");
                this.fireCloseEvent(true);
            } else {
                logger.exception(e);
            }
        }

        private void fireCloseEvent(boolean remote) {
            if (listener != null) {
                // So the user can immediately reconnect without
                // errors from the underlying library.
                new Thread(() -> listener.onClose(remote)).start();
            }
        }

    }

    public void sendChat(@NonNull String message, @NonNull KoiChatterType chatter) {
        this.socket.send(
            new JsonObject()
                .put("type", "CHAT")
                .put("message", message)
                .put("chatter", chatter.name())
                .toString()
        );
    }

    public void upvoteChat(@NonNull String messageId) {
        this.socket.send(
            new JsonObject()
                .put("type", "UPVOTE")
                .put("message_id", messageId)
                .toString()
        );
    }

    public void deleteChat(@NonNull String messageId) {
        this.socket.send(
            new JsonObject()
                .put("type", "DELETE")
                .put("message_id", messageId)
                .toString()
        );
    }

    public void sendTest(@NonNull String eventType) {
        this.socket.send(
            new JsonObject()
                .put("type", "TEST")
                .put("eventType", eventType.toUpperCase())
                .toString()
        );
    }

    public void sendStreamUpdate(@NonNull KoiStreamConfiguration config) {
        this.socket.send(
            new JsonObject()
                .put("type", "UPDATE_STREAM")
                .put("stream", Rson.DEFAULT.toJson(config))
                .toString()
        );
    }

}

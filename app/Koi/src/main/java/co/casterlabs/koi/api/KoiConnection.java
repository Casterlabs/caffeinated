package co.casterlabs.koi.api;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.events.CatchupEvent;
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

    @SneakyThrows
    public KoiConnection(@NonNull String url, @NonNull FastLogger logger, @NonNull KoiLifeCycleHandler listener, String clientId) {
        this.logger = logger;
        this.listener = listener;
        this.uri = new URI(url + "?client_id=" + clientId);
    }

    @Override
    public void close() {
        if (this.isConnected()) {
            this.socket.close();
        }
    }

    public boolean isConnected() {
        return this.socket != null;
    }

    public KoiConnection hookStreamStatus(String username, UserPlatform platform) throws InterruptedException, IOException {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected.");
        }

        JsonObject request = new JsonObject()
            .put("type", "USER_STREAM_STATUS")
            .put("username", username)
            .put("platform", platform.name())
            .put("nonce", "_login");

        try {
            this.socket = new KoiSocket(this.uri, request);
            this.socket.doConnect();
        } catch (InterruptedException | IOException e) {
            socket = null;
            throw e;
        }

        return this;
    }

    public KoiConnection login(String token) throws InterruptedException, IOException {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected.");
        }

        JsonObject request = new JsonObject()
            .put("type", "LOGIN")
            .put("token", token)
            .put("nonce", "_login");

        try {
            this.socket = new KoiSocket(this.uri, request);
            this.socket.doConnect();
        } catch (InterruptedException | IOException e) {
            socket = null;
            throw e;
        }

        return this;
    }

    public KoiConnection loginPuppet(String token) {
        if (!this.isConnected()) {
            throw new IllegalStateException("You need to be connected.");
        }

        JsonObject request = new JsonObject()
            .put("type", "PUPPET_LOGIN")
            .put("token", token)
            .put("nonce", "_puppetlogin");

        this.socket.send(request.toString());

        return this;
    }

    private class KoiSocket extends WebSocketClient {
        private final JsonObject loginRequest;
        private boolean isFresh = true;

        public KoiSocket(URI uri, JsonObject loginRequest) {
            super(uri);
            this.loginRequest = loginRequest;

            this.setConnectionLostTimeout(60 /* Seconds */);
            this.addHeader("User-Agent", "Casterlabs");
            this.setTcpNoDelay(true);
        }

        public void doConnect() throws InterruptedException, IOException {
            logger.info("Connecting to Koi...");
            boolean success = super.connectBlocking();
            if (!success) throw new IOException("Failed to connect.");
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            logger.info("Connected to Koi, waiting for welcome.");
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
                    case "WELCOME": {
                        logger.info("Got welcome: %s", packet);
                        this.send(this.loginRequest.toString());
                        listener.onOpen();
                        return;
                    }

                    case "KEEP_ALIVE": {
                        this.keepAlive(packet.get("nonce"));
                        return;
                    }

                    case "INTEGRATION_FEATURES": {
                        listener.onSupportedFeatures(
                            Rson.DEFAULT.fromJson(
                                packet.get("features"),
                                new TypeToken<List<KoiIntegrationFeatures>>() {
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
                        }

                        if ((event instanceof CatchupEvent) && isFresh) {
                            ((CatchupEvent) event).setFresh(true);
                            isFresh = false;
                        }

                        KoiEventUtil.reflectInvoke(listener, event);
                        return;
                    }

                    // We don't care about these or we already have them.
                    case "CLIENT_SCOPES":
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
            socket = null;

            if (remote) {
                logger.info("Lost connection to Koi.");
            } else {
                logger.info("Disconnected from Koi. (%s)", reason);
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

    public void sendChat(@NonNull String message, @NonNull KoiChatterType chatter, @Nullable String replyTarget, boolean isUserGesture) {
        this.socket.send(
            new JsonObject()
                .put("type", "CHAT")
                .put("message", message)
                .put("chatter", chatter.name())
                .put("reply_target", replyTarget)
                .put("is_user_gesture", isUserGesture)
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

    public void deleteChat(@NonNull String messageId, boolean isUserGesture) {
        this.socket.send(
            new JsonObject()
                .put("type", "DELETE")
                .put("message_id", messageId)
                .put("is_user_gesture", isUserGesture)
                .toString()
        );
    }

}

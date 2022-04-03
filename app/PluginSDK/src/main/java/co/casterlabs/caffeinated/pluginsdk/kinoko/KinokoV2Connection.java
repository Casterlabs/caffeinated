package co.casterlabs.caffeinated.pluginsdk.kinoko;

import java.io.Closeable;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KinokoV2Connection implements Closeable {
    private static final String KINOKOV2_URL = "wss://api.casterlabs.co/v2/kinoko/%s";
    private static volatile int connectionCount = 0;

    private @Nullable @Getter FastLogger logger;

    private KinokoV2Listener listener;
    private KinokoSocket socket;

    private @Getter String id;
    private List<String> channelMembers;

    public KinokoV2Connection(@NonNull KinokoV2Listener listener) {
        this.listener = listener;
    }

    @SneakyThrows
    public void connect(@NonNull String channel) throws InterruptedException {
        if (!this.isConnected()) {
            this.id = null;
            this.channelMembers = new LinkedList<>();

            this.logger = new FastLogger(String.format("KinokoV2 (%s, %d)", channel, connectionCount++));

            @SuppressWarnings("deprecation")
            String uri = String.format(
                KINOKOV2_URL,
                URLEncoder.encode(channel)
            );

            this.socket = new KinokoSocket(new URI(uri));
            this.socket.connectBlocking();
        }
    }

    public List<String> getChannelMembers() {
        return new ArrayList<>(this.channelMembers);
    }

    public void send(@NonNull JsonObject data, @Nullable String target) {
        if (this.isConnected()) {
            this.socket.send(data, target);
        }
    }

    @Override
    public void close() {
        if (this.isConnected()) {
            this.socket.close();
        }
    }

    public boolean isConnected() {
        return (this.socket != null) && this.socket.isOpen();
    }

    private class KinokoSocket extends WebSocketClient {

        public KinokoSocket(URI uri) {
            super(uri);

            this.addHeader("User-Agent", "Casterlabs");
            this.setTcpNoDelay(true);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            logger.log(LogLevel.TRACE, "\u2713 Open");
        }

        @Override
        public void send(String text) {
            logger.log(LogLevel.TRACE, "\u2191 " + text);
            super.send(text);
        }

        public void send(JsonObject data, @Nullable String target) {
            this.send(
                new JsonObject()
                    .put("type", "MESSAGE")
                    .put("payload", data)
                    .put("target", target)
                    .toString()
            );
        }

        public void ping() {
            this.send(
                new JsonObject()
                    .put("type", "PING")
                    .put("payload", new JsonObject())
                    .toString()
            );
        }

        @SneakyThrows
        @Override
        public void onMessage(String raw) {
            logger.log(LogLevel.TRACE, "\u2193 " + raw);

            JsonObject payload = Rson.DEFAULT.fromJson(raw, JsonObject.class);
            ChannelMessageType type = Rson.DEFAULT.fromJson(payload.get("type"), ChannelMessageType.class);

            if (type == ChannelMessageType.ERROR) {
                // TODO ?
                logger.severe("An error was received: %s", payload.getArray("errors"));
            } else {
                JsonObject data = payload.getObject("data");

                switch (type) {

                    case ID: {
                        String id = data.getString("id");
                        KinokoV2Connection.this.id = id;
                        listener.onOpen(id);
                        break;
                    }

                    case JOIN: {
                        String id = data.getString("id");
                        channelMembers.add(id);
                        listener.onJoin(id);
                        break;
                    }

                    case LEAVE: {
                        String id = data.getString("id");
                        channelMembers.remove(id);
                        listener.onLeave(id);
                        break;
                    }

                    case LIST: {
                        for (JsonElement e : data.getArray("list")) {
                            channelMembers.add(e.getAsString());
                        }
                        break;
                    }

                    case MESSAGE: {
                        String sender = payload.getString("sender");
                        JsonObject message = data;

                        listener.onMessage(message, sender);
                        break;
                    }

                    case PING: {
                        this.ping();
                        break;
                    }

                    case ERROR:
                        break;

                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            logger.log(LogLevel.TRACE, "x Closed");
            // So the user can immediately reconnect without
            // errors from the underlying library.
            new Thread(() -> listener.onClose(remote)).start();
        }

        @Override
        public void onError(Exception e) {
            listener.onException(e);
        }

    }

    private static enum ChannelMessageType {
        MESSAGE,
        JOIN,
        LEAVE,
        LIST,
        PING,
        ERROR,
        ID;

    }

}

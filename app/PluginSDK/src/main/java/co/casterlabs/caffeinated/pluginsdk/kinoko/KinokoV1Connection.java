package co.casterlabs.caffeinated.pluginsdk.kinoko;

import java.io.Closeable;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.WebUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KinokoV1Connection implements Closeable {
    private static final String KINOKOV1_URL = "wss://api.casterlabs.co/v1/kinoko?channel=%s&type=%s&proxy=%b";

    private @Nullable @Getter FastLogger logger;

    private KinokoV1Listener listener;
    private KinokoSocket socket;

    public KinokoV1Connection(@NonNull KinokoV1Listener listener) {
        this.listener = listener;
    }

    @SneakyThrows
    public void connect(@NonNull String channel, boolean isParent, boolean shouldProxy) throws InterruptedException {
        if (!this.isConnected()) {
            String typeString = isParent ? "parent" : "client";

            this.logger = new FastLogger(String.format("KinokoV1 (%s) %s", channel, typeString));

            String uri = String.format(
                KINOKOV1_URL,
                WebUtil.encodeURIComponent(channel),
                typeString,
                shouldProxy
            );

            this.socket = new KinokoSocket(new URI(uri));
            this.socket.connectBlocking();
        }
    }

    public void send(String message) {
        if (this.isConnected()) {
            this.socket.send(message);
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
            listener.onOpen();
        }

        @Override
        public void send(String text) {
            logger.log(LogLevel.TRACE, "\u2191 " + text);

            super.send(text);
        }

        private void keepAlive() {
            this.send(":ping");
        }

        @Override
        public void onMessage(String data) {
            logger.log(LogLevel.TRACE, "\u2193 " + data);

            switch (data) {
                case ":ping": {
                    this.keepAlive();
                    return;
                }

                case ":orphaned": {
                    listener.onOrphaned();
                    return;
                }

                case ":adopted": {
                    listener.onAdopted();
                    return;
                }

                default: {
                    listener.onMessage(data);
                    return;
                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            // So the user can immediately reconnect without
            // errors from the underlying library.
            new Thread(() -> listener.onClose(remote)).start();
        }

        @Override
        public void onError(Exception e) {
            listener.onException(e);
        }

    }

}

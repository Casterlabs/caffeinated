package co.casterlabs.caffeinated.localserver;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.util.ModifiableArray;
import co.casterlabs.caffeinated.localserver.handlers.RouteLocalServer;
import co.casterlabs.caffeinated.localserver.handlers.RouteMiscApi;
import co.casterlabs.caffeinated.localserver.handlers.RoutePluginApi;
import co.casterlabs.caffeinated.localserver.handlers.RouteWidgetApi;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeConnection;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rhs.HttpMethod;
import co.casterlabs.rhs.HttpServer;
import co.casterlabs.rhs.HttpServerBuilder;
import co.casterlabs.rhs.protocol.api.ApiFramework;
import co.casterlabs.rhs.protocol.http.HttpProtocol;
import co.casterlabs.rhs.protocol.websocket.Websocket;
import co.casterlabs.rhs.protocol.websocket.WebsocketProtocol;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class LocalServer implements Closeable {
    public static final String ALLOWED_METHODS;

    private static final long PING_INTERVAL = TimeUnit.SECONDS.toMillis(15);
    private HttpServer server;

    public static final ModifiableArray<Websocket> websockets = new ModifiableArray<>((c) -> new Websocket[c]);

    static {
        List<String> methods = new ArrayList<>();
        for (HttpMethod method : HttpMethod.values()) {
            methods.add(method.name());
        }

        ALLOWED_METHODS = String.join(", ", methods);
    }

    @SneakyThrows
    public LocalServer(int port) {
        ApiFramework framework = new ApiFramework();
        framework.register(new RouteLocalServer());
        framework.register(new RouteMiscApi());
        framework.register(new RoutePluginApi());
        framework.register(new RouteWidgetApi());

        this.server = new HttpServerBuilder()
            .withPort(port)
            .withBehindProxy(false)
            .withKeepAliveSeconds(-1)
            .withMinSoTimeoutSeconds(120)
            .withServerHeader("Casterlabs-Caffeinated/1")
            .with(new HttpProtocol(), framework.httpHandler)
            .with(new WebsocketProtocol(), framework.websocketHandler)
            .build();

        this.server.logger()
            .setCurrentLevel(LogLevel.SEVERE);

        AsyncTask.create(this::pingHandler);
    }

    private void pingHandler() {
        while (true) {
            try {
                for (Websocket websocket : websockets.get()) {
                    try {
                        Pair<RealtimeConnection, Object> attachment = websocket.attachment();

                        if (attachment != null) {
                            attachment.a().checkExpiryAndPing();
                        }
                    } catch (Throwable ignored) {}
                }
                Thread.sleep(PING_INTERVAL);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /* ---------------- */
    /* IO Related       */
    /* ---------------- */

    public void start() throws IOException {
        this.server.start();
        FastLogger.logStatic("Started!");
    }

    public boolean isAlive() {
        return this.server.isAlive();
    }

    @Override
    public void close() throws IOException {
        this.server.stop(true);
        FastLogger.logStatic("Stopped!");
    }

}

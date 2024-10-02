package co.casterlabs.caffeinated.localserver;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.localserver.handlers.RouteLocalServer;
import co.casterlabs.caffeinated.localserver.handlers.RouteMiscApi;
import co.casterlabs.caffeinated.localserver.handlers.RoutePluginApi;
import co.casterlabs.caffeinated.localserver.handlers.RouteWidgetApi;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeConnection;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rhs.protocol.HttpMethod;
import co.casterlabs.rhs.session.Websocket;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class LocalServer implements Closeable, HttpProvider {
    public static final String ALLOWED_METHODS;

    private static final long PING_INTERVAL = TimeUnit.SECONDS.toMillis(15);
    private SoraFramework framework;
    private int port;

    static {
        List<String> methods = new ArrayList<>();
        for (HttpMethod method : HttpMethod.values()) {
            methods.add(method.name());
        }

        ALLOWED_METHODS = String.join(", ", methods);
    }

    @SneakyThrows
    public LocalServer(int port) {
        this.port = port;
        this.framework = new SoraLauncher()
            .setPort(this.port)
            .setBindAddress("0.0.0.0") // TODO investigate using ::
            .buildWithoutPluginLoader();

        this.framework
            .getServer()
            .getLogger()
            .setCurrentLevel(LogLevel.SEVERE);

        this.framework
            .getSora()
            .register(new LocalServerPluginWrapper());
    }

    private class LocalServerPluginWrapper extends SoraPlugin {

        @Override
        public void onInit(Sora sora) {
            sora.addProvider(this, new RouteLocalServer());
            sora.addProvider(this, new RouteMiscApi());
            sora.addProvider(this, new RoutePluginApi());
            sora.addProvider(this, new RouteWidgetApi());

            AsyncTask.create(this::pingHandler);
        }

        private void pingHandler() {
            while (true) {
                try {
                    for (Websocket websocket : this.getWebsockets()) {
                        try {
                            Pair<RealtimeConnection, Object> attachment = websocket.getAttachment();

                            if (attachment != null) {
                                attachment.a().checkExpiryAndPing();
                            }
                        } catch (ClassCastException ignored) {}
                    }
                    Thread.sleep(PING_INTERVAL);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        @Override
        public void onClose() {}

        @Override
        public @Nullable String getVersion() {
            return CaffeinatedApp.getInstance().getBuildInfo().getVersionString();
        }

        @Override
        public @Nullable String getAuthor() {
            return "Casterlabs";
        }

        @Override
        public @NonNull String getName() {
            return "Caffeinated Conductor (LocalServer)";
        }

        @Override
        public @NonNull String getId() {
            return "co.casterlabs.caffeinated.conductor";
        }

    }

    /* ---------------- */
    /* IO Related       */
    /* ---------------- */

    public void start() throws IOException {
        this.framework.getServer().start();
        FastLogger.logStatic("Started!");
    }

    public boolean isAlive() {
        return this.framework.getServer().isAlive();
    }

    @Override
    public void close() throws IOException {
        this.framework.getServer().stop();
        FastLogger.logStatic("Stopped!");
    }

}

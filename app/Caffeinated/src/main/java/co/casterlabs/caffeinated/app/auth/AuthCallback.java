package co.casterlabs.caffeinated.app.auth;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Listener;
import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.promise.Promise;
import co.casterlabs.commons.async.promise.PromiseResolver;
import lombok.Getter;
import lombok.SneakyThrows;

public class AuthCallback {
    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(10);

    private @Getter String stateString;
    private String type;

    private KinokoV1Connection connection;

    private Promise<String> authPromise;
    private Consumer<String> authPromiseResolve;
    private Consumer<Throwable> authPromiseReject;

    private AsyncTask timeoutWatcher;

    public AuthCallback(String type, boolean isKoi) {
        this.connection = new KinokoV1Connection(new Listener());
        this.type = type;

        // TODO get rid of the "caffeinated_"
        this.stateString = String.format("auth_redirect:%s:caffeinated_%s", new String(Crypto.generateSecureRandomKey()), this.type);

        if (isKoi) {
            this.stateString += ':' + CaffeinatedApp.KOI_ID;
        }
    }

    public Promise<String> connect() {
        if (this.authPromise == null) {
            this.authPromise = new Promise<>((PromiseResolver<String> resolver) -> {
                this.authPromiseResolve = resolver::resolve;
                this.authPromiseReject = resolver::reject;

                this.timeoutWatcher = AsyncTask.create(() -> {
                    try {
                        Thread.sleep(TIMEOUT);
                        this.authPromiseReject.accept(new AuthException("TIMED_OUT"));
                        cancel();
                    } catch (InterruptedException ignored) {}
                });
            });

            this.reconnect();
        }

        return this.authPromise;
    }

    public void cancel() {
        this.authPromiseResolve.accept(null);
        this.timeoutWatcher.cancel();
        this.connection.close();
    }

    @SneakyThrows
    private void reconnect() {
        this.connection.connect(this.stateString, true, false);
    }

    public static class AuthException extends Exception {
        private static final long serialVersionUID = 8406841913406112449L;

        public AuthException(String reason) {
            super(reason);
        }

    }

    private class Listener implements KinokoV1Listener {

        @Override
        public void onOpen() {}

        @Override
        public void onOrphaned() {}

        @Override
        public void onAdopted() {}

        @Override
        public void onMessage(String message) {
            if (message.startsWith("\"token:")) {
                String token = message.substring(7, message.length() - 1);

                authPromiseResolve.accept(token);
                cancel();
            } else {
                authPromiseReject.accept(new AuthException("NO_TOKEN_PROVIDED"));
                cancel();
            }
        }

        @Override
        public void onClose(boolean remote) {
            if (remote) {
                reconnect();
            }
        }

    }

}

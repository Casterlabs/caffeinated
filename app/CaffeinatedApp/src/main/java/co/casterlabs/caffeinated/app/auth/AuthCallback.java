package co.casterlabs.caffeinated.app.auth;

import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Listener;
import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.kaimen.util.threading.Promise;
import lombok.Getter;
import lombok.SneakyThrows;

public class AuthCallback {
    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(10);

    private @Getter String stateString;
    private String type;

    private KinokoV1Connection connection;

    private Promise<String> authPromise;
    private AsyncTask timeoutWatcher;

    public AuthCallback(String type) {
        this.connection = new KinokoV1Connection(new Listener());
        this.type = type;

        this.stateString = String.format("auth_redirect:%s:%s", new String(Crypto.generateSecureRandomKey()), this.type);
    }

    public Promise<String> connect() {
        if (this.authPromise == null) {
            this.authPromise = new Promise<>();

            this.timeoutWatcher = new AsyncTask(() -> {
                try {
                    Thread.sleep(TIMEOUT);
                    authPromise.error(new AuthException("TIMED_OUT"));
                    cancel();
                } catch (InterruptedException ignored) {}
            });

            this.reconnect();
        }

        return this.authPromise;
    }

    public void cancel() {
        this.authPromise.fulfill(null);
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

                authPromise.fulfill(token);
                cancel();
            } else {
                authPromise.error(new AuthException("NO_TOKEN_PROVIDED"));
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

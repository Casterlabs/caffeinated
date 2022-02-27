package co.casterlabs.caffeinated.localserver.websocket;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public abstract class RealtimeConnection implements Closeable {
    private static final long EXPIRATION_TIME = TimeUnit.SECONDS.toMillis(30);

    private long lastPong = System.currentTimeMillis();

    protected abstract void ping();

    public void resetTimeout() {
        this.lastPong = System.currentTimeMillis();
    }

    public void checkExpiryAndPing() {
        try {
            if ((System.currentTimeMillis() - this.lastPong) > EXPIRATION_TIME) {
                this.close();
            } else {
                this.ping();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}

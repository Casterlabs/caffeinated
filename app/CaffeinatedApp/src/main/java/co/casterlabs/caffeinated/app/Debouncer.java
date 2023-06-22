package co.casterlabs.caffeinated.app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Debouncer {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> last;

    public void debounce(final Runnable runnable) {
        if (this.last != null && !this.last.isDone()) {
            this.last.cancel(false);
        }

        this.last = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, 250, TimeUnit.MILLISECONDS);
    }
}

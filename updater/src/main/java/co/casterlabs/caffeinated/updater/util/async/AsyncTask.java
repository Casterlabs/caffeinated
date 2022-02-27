package co.casterlabs.caffeinated.updater.util.async;

import lombok.NonNull;

public class AsyncTask {
    private static int taskId = 0;

    private Thread t;

    public AsyncTask(@NonNull Runnable run) {
        this.t = new Thread(run);

        this.t.setName("AsyncTask #" + taskId++);
        this.t.setDaemon(true);
        this.t.start();
    }

    @SuppressWarnings("deprecation")
    public void cancel() {
        this.t.stop();
    }

}

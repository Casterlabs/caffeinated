package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.Promise;
import xyz.e3ndr.consoleutil.ipc.IpcChannel;
import xyz.e3ndr.consoleutil.ipc.MemoryMappedIpc;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class InstanceManager {
    private static File ipcDir = new File(CaffeinatedApp.appDataDir, "/ipc/");
    private static File lockFile = new File(ipcDir, "instance.lock");

    static {
        ipcDir.mkdirs();
    }

    private static RandomAccessFile randomAccessFile;
    private static FileLock fileLock;

    private static boolean hostStarted = false;

    public static boolean isSingleInstance() {
        try {
            lockFile.createNewFile();

            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            fileLock = randomAccessFile.getChannel().tryLock();

            if (fileLock == null) {
                // We could not lock the file, therefore another instance has already locked it.
                return false;
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    cleanShutdown();
                }
            });

            return true;
        } catch (Exception e) {
            FastLogger.logException(e);
            return true; // It's probably safe to continue.
        }
    }

    public static void cleanShutdown() {
        if (fileLock != null) {
            try {
                fileLock.release();
            } catch (Exception e) {}
        }

        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (Exception e) {}
        }
    }

    public static boolean trySummonInstance() {
        return childIpcComms("SUMMON");
    }

    public static void closeOtherInstance() {
        childIpcComms("SHUTDOWN");
    }

    private static boolean childIpcComms(String command) {
        Promise<Void> commsPromise = new Promise<>(new BiConsumer<Consumer<Void>, Consumer<Throwable>>() {
            private volatile boolean completed = false;

            @Override
            public void accept(Consumer<Void> resolve, Consumer<Throwable> reject) {
                AsyncTask ipcTask = AsyncTask.create(() -> {
                    try {
                        IpcChannel ipc = MemoryMappedIpc.startHostIpc(ipcDir, "launch");

                        ipc.write(command);

                        while (true) {
                            String line = ipc.read();

                            FastLogger.logStatic("IPC (CHILD): %s", line);

                            if (line.equals("OK")) {
                                resolve.accept(null);
                                break;
                            }
                        }

                        ipc.close();
                    } catch (Exception e) {
                        reject.accept(new IOException("IPC communications failed.", e));
                    } finally {
                        this.completed = true;
                    }
                });

                // Watchdog task
                AsyncTask.create(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {}

                    if (!this.completed) {
                        ipcTask.cancel();
                        reject.accept(new IllegalStateException("IPC communication took more than 5 seconds."));
                    }
                });
            }
        });

        commsPromise.except((e) -> {
            // Ignored.
        });

        try {
            commsPromise.await();
            return true;
        } catch (Throwable e) {
//            FastLogger.logException(e);
            return false;
        }
    }

    // <START>
    // > SUMMON
    // < OK
    // <END>
    public static void startIpcHost() {
        if (hostStarted) {
            return;
        } else {
            hostStarted = false;
        }

        try {
            new Promise<>((resolve, reject) -> {
                try {
                    IpcChannel ipc = MemoryMappedIpc.startChildIpc(ipcDir, "launch");
                    resolve.accept(null);

                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            try {
                                ipc.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    while (true) {
                        String line = ipc.read();

                        FastLogger.logStatic("IPC (HOST): %s", line);

                        if (line.equals("SUMMON")) {
                            if (!Bootstrap.getWebview().isOpen()) {
                                Bootstrap.getWebview().open(CaffeinatedApp.getInstance().getAppUrl());
                            }
                            Bootstrap.getWebview().focus();
                        } else if (line.equals("SHUTDOWN")) {
                            Bootstrap.shutdown();
                        }

                        ipc.write("OK");
                    }
                } catch (Exception e) {
                    reject.accept(e);
                }
            })
                .await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}

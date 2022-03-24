package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.kaimen.util.threading.Promise;
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
        Promise<Void> commsPromise = new Promise<>();

        // Watchdog task
        new AsyncTask(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {}

            if (!commsPromise.isCompleted()) {
                commsPromise.error(new IllegalStateException("IPC communication took more than 5 seconds."));
            }
        });

        // IPC task
        new AsyncTask(() -> {
            try {
                IpcChannel ipc = MemoryMappedIpc.startHostIpc(ipcDir, "launch");

                ipc.write(command);

                while (true) {
                    String line = ipc.read();

                    FastLogger.logStatic("IPC (CHILD): %s", line);

                    if (line.equals("OK")) {
                        commsPromise.fulfill(null);
                        break;
                    }
                }

                ipc.close();
            } catch (Exception e) {
                commsPromise.error(new IOException("IPC communications failed.", e));
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

        Promise<Void> commsPromise = new Promise<>();

        new AsyncTask(() -> {
            try {
                IpcChannel ipc = MemoryMappedIpc.startChildIpc(ipcDir, "launch");
                commsPromise.fulfill(null);

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
                commsPromise.fulfill(null);
            }
        });

        try {
            commsPromise.await();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}

package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import xyz.e3ndr.consoleutil.ipc.IpcChannel;
import xyz.e3ndr.consoleutil.ipc.MemoryMappedIpc;

public class InstanceManager {
    private static File ipcDir = new File(CaffeinatedApp.appDataDir, "/ipc/");
    private static File lockFile = new File(ipcDir, "instance.lock");

    static {
        ipcDir.mkdirs();
    }

    private static RandomAccessFile randomAccessFile;
    private static FileLock fileLock;

    public static boolean isSingleInstance() {
        if (!lockFile.exists()) {
            try {
                randomAccessFile = new RandomAccessFile(lockFile, "rw");
                fileLock = randomAccessFile.getChannel().tryLock();

                if (fileLock != null) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            cleanShutdown();
                        }
                    });

                    startIpcHost();
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static void cleanShutdown() {
        if (lockFile != null) {
            try {
                lockFile.delete();
            } catch (Exception e) {}
        }

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
        childIpcComms("CLOSE");
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
    private static void startIpcHost() {
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

                    if (line.equals("SUMMON")) {
                        Bootstrap.getWebview().open(CaffeinatedApp.getInstance().getAppUrl());
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

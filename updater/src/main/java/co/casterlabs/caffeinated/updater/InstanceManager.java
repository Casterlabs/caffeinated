package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.updater.util.async.AsyncTask;
import co.casterlabs.caffeinated.updater.util.async.Promise;
import xyz.e3ndr.consoleutil.ipc.IpcChannel;
import xyz.e3ndr.consoleutil.ipc.MemoryMappedIpc;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class InstanceManager {
    private static File ipcDir = new File(Updater.appDataDirectory, "/ipc/");
    private static File lockFile = new File(ipcDir, "instance.lock");

    private static final long CHECK_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    static {
        ipcDir.mkdirs();
    }

    private static RandomAccessFile randomAccessFile;
    private static FileLock fileLock;

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
        childIpcComms("SHUTDOWN");
    }

    private static boolean childIpcComms(String command) {
        Promise<Void> commsPromise = new Promise<>();

        // Watchdog task
        new AsyncTask(() -> {
            try {
                Thread.sleep(CHECK_TIMEOUT);
            } catch (InterruptedException ignored) {}

            if (!commsPromise.isCompleted()) {
                commsPromise.error(new IllegalStateException("IPC communication timed out."));
                FastLogger.logStatic(LogLevel.INFO, "IPC communication timed out.");
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

}

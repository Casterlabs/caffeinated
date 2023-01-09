package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.PromiseWithHandles;
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
        PromiseWithHandles<Void> commsPromise = new PromiseWithHandles<>();

        // Watchdog task
        AsyncTask.create(() -> {
            try {
                Thread.sleep(CHECK_TIMEOUT);
            } catch (InterruptedException ignored) {}

            if (!commsPromise.hasCompleted()) {
                commsPromise.reject(new IllegalStateException("IPC communication timed out."));
                FastLogger.logStatic(LogLevel.INFO, "IPC communication timed out.");
            }
        });

        // IPC task
        AsyncTask.create(() -> {
            try {
                IpcChannel ipc = MemoryMappedIpc.startHostIpc(ipcDir, "launch");

                ipc.write(command);

                while (true) {
                    String line = ipc.read();

                    if (line.equals("OK")) {
                        commsPromise.resolve(null);
                        break;
                    }
                }

                ipc.close();
            } catch (Exception e) {
                commsPromise.reject(new IOException("IPC communications failed.", e));
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

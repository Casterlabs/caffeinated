package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.util.ZipUtil;
import co.casterlabs.caffeinated.updater.util.platform.OperatingSystem;
import co.casterlabs.caffeinated.updater.util.platform.Platform;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import net.harawata.appdirs.AppDirsFactory;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Updater {
    private static final int VERSION = 22;
    private static final String CHANNEL = "stable";

    private static String REMOTE_ZIP_DOWNLOAD_URL = "https://cdn.casterlabs.co/dist/" + CHANNEL + "/";
    private static final String REMOTE_COMMIT_URL = "https://cdn.casterlabs.co/dist/" + CHANNEL + "/commit";
    private static final String LAUNCHER_VERSION_URL = "https://cdn.casterlabs.co/dist/updater-version";

    public static String appDataDirectory = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    private static File appDirectory = new File(appDataDirectory, "app");
    private static File updateFile = new File(appDirectory, "update.zip");
    private static File commitFile = new File(appDirectory, ".commit");
    private static File buildokFile = new File(appDirectory, ".build_ok");

    private static final List<OperatingSystem> INLINE_PLATFORMS = Arrays.asList(
//        OperatingSystem.WINDOWS
    );

    private static @Getter boolean isLauncherOutOfDate = false;
    private static @Getter boolean isPlatformSupported = true;

    private static final String launchCommand;

    static {
        appDirectory.mkdirs();

        switch (Platform.os) {
            case MACOSX:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated.app/Contents/MacOS/Casterlabs-Caffeinated";
                REMOTE_ZIP_DOWNLOAD_URL += "macOS-amd64.zip";
                break;

            case LINUX:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated";
                REMOTE_ZIP_DOWNLOAD_URL += "Linux-amd64.zip";
                break;

            case WINDOWS:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated.exe";
                REMOTE_ZIP_DOWNLOAD_URL += "Windows-amd64.zip";
                break;

            default:
                launchCommand = null;
                isPlatformSupported = false;
                break;
        }

        try {
            int remoteLauncherVersion = Integer.parseInt(WebUtil.sendHttpRequest(new Request.Builder().url(LAUNCHER_VERSION_URL)).trim());

            isLauncherOutOfDate = VERSION < remoteLauncherVersion;
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    public static void borkInstall() {
        buildokFile.delete();
    }

    public static boolean needsUpdate() {
        try {
            // Check for existence of files.
            if (!commitFile.exists()) {
                return true;
            } else if (!buildokFile.exists()) {
                FastLogger.logStatic("Build was not healthy, forcing redownload.");
                return true;
            } else {
                // Check the version.
                String installedCommit = FileUtil.readFile(commitFile).trim();
                String remoteCommit = WebUtil.sendHttpRequest(new Request.Builder().url(REMOTE_COMMIT_URL)).trim();

                return !remoteCommit.equals(installedCommit);
            }
        } catch (IOException e) {
            FastLogger.logException(e);
            return true;
        }
    }

    public static void downloadAndInstallUpdate(UpdaterDialog dialog) throws UpdaterException {
        FileUtil.emptyDirectory(appDirectory);

        try (Response response = WebUtil.sendRawHttpRequest(new Request.Builder().url(REMOTE_ZIP_DOWNLOAD_URL))) {
            // Download zip.
            {
                dialog.setStatus("Downloading updates...");

                InputStream source = response.body().byteStream();
                OutputStream dest = new FileOutputStream(updateFile);

                double totalSize = response.body().contentLength();
                int totalRead = 0;

                byte[] buffer = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
                int read = 0;

                while ((read = source.read(buffer)) != -1) {
                    dest.write(buffer, 0, read);
                    totalRead += read;

                    double progress = totalRead / totalSize;

                    dialog.setStatus(String.format("Downloading updates... (%.0f%%)", progress * 100));
                    dialog.setProgress(progress);
                }

                dest.flush();

                source.close();
                dest.close();

                dialog.setProgress(-1);
            }

            // Extract zip
            {
                dialog.setStatus("Installing updates...");
                ZipUtil.unzip(updateFile, appDirectory);

                commitFile.createNewFile();

                String remoteCommit = WebUtil.sendHttpRequest(new Request.Builder().url(REMOTE_COMMIT_URL)).trim();
                Files.writeString(commitFile.toPath(), remoteCommit);

                updateFile.delete();

                // Make the executable... executable on Linux.
                if (Platform.os == OperatingSystem.LINUX) {
                    String executable = appDirectory.getAbsolutePath() + "/Casterlabs-Caffeinated";

                    new ProcessBuilder()
                        .command(
                            "chmod", "+x", executable
                        )
                        .inheritIO()
                        .start()

                        // Wait for exit.
                        .waitFor();
                } else

                    // Unquarantine the app on MacOS.
                    if (Platform.os == OperatingSystem.MACOSX) {
                        String app = '"' + appDirectory.getAbsolutePath() + "/Casterlabs-Caffeinated.app" + '"';
                        String command = "xattr -rd com.apple.quarantine " + app + " && chmod -R u+x " + app;

                        dialog.setStatus("Waiting for permission...");

                        new ProcessBuilder()
                            .command(
                                "osascript",
                                "-e",
                                "do shell script \"" + command.replace("\"", "\\\"") + "\" with prompt \"Casterlabs Caffeinated would like to make changes.\" with administrator privileges"
                            )
                            .inheritIO()
                            .start()

                            // Wait for exit.
                            .waitFor();
                    }
            }
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.DOWNLOAD_FAILED, "Update failed :(", e);
        }
    }

    public static void launch(UpdaterDialog dialog) throws UpdaterException {
        try {
            if (INLINE_PLATFORMS.contains(Platform.os)) {
                // Here's where the fun starts, we load all the jars and run the in the same
                // process 👀

                // Change directory to the app dir, otherwise the app will pollute the updater's
                // installation directory and the world would melt.
                ChangeDir.changeProcessDir(appDirectory);

                // Load and parse the manifest.
                String manifestContent = FileUtil.readFile(new File(appDirectory, "Casterlabs-Caffeinated.json"))
                    .replace("\n", "");

                JsonObject manifest = Rson.DEFAULT.fromJson(
                    manifestContent,
                    JsonObject.class
                );

                JsonArray classpathDecl = manifest.getArray("classPath");

                URL[] classpath = new URL[classpathDecl.size()];
                String mainClassName = manifest.getString("mainClass");
                String[] args = new String[0];

                for (int idx = 0; idx < classpath.length; idx++) {
                    classpath[idx] = new File(appDirectory, classpathDecl.getString(idx))
                        .toURI()
                        .toURL();
                }

                // Load the app's classpath but keep it separated from the updater's
                ClassLoader loader = new URLClassLoader(classpath, ClassLoader.getPlatformClassLoader());
                Class<?> mainClass = Class.forName(mainClassName, true, loader);
                Method mainMethod = mainClass.getMethod("main", String[].class);

                // Hide the updater dialog, invoke the main method, and pray.
                dialog.dispose();
                mainMethod.invoke(null, (Object) args);
            } else {
                ProcessBuilder pb = new ProcessBuilder()
                    .directory(appDirectory)
                    .command(launchCommand);

                // TODO wait for .build_ok to show up.

                if (Platform.os == OperatingSystem.MACOSX) {
                    // On MacOS we do not want to keep the updater process open as it'll stick in
                    // the dock. So we start the process and kill the updater to make sure that
                    // doesn't happen.
                    FastLogger.logStatic(LogLevel.INFO, "The process will now exit, this is so the updater's icon doesn't stick in the dock.");
                    pb.start();
                    dialog.close();
                    return;
                }

                pb.start();

                // TODO look for "Starting the UI" before we close the dialog.
                // TODO look for the .build_ok file before trusting the process. (kill & let the
                // user know it's dead)

                TimeUnit.SECONDS.sleep(2);
                dialog.close();
                System.exit(0);
            }
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.LAUNCH_FAILED, "Could not launch update :(", e);
        }
    }

}

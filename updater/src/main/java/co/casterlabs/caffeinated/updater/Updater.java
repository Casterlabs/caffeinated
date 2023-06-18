package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.util.ZipUtil;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import net.harawata.appdirs.AppDirsFactory;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Updater {
    private static final int VERSION = 29;
    private static final String CHANNEL = System.getProperty("caffeinated.channel", "stable");

    private static String REMOTE_ZIP_DOWNLOAD_URL = "https://cdn.casterlabs.co/dist/" + CHANNEL + "/";
    private static final String REMOTE_COMMIT_URL = "https://cdn.casterlabs.co/dist/" + CHANNEL + "/commit";
    private static final String LAUNCHER_VERSION_URL = "https://cdn.casterlabs.co/dist/updater-version";

    public static String appDataDirectory = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    private static File appDirectory = new File(appDataDirectory, "app");
    private static File updateFile = new File(appDirectory, "update.zip");
    private static File buildInfoFile = new File(appDirectory, "current_build_info.json");
    private static File expectUpdaterFile = new File(appDirectory, "expect-updater");

    private static final List<OSDistribution> NO_JRE_DISTS = Arrays.asList(OSDistribution.WINDOWS_NT);

    private static @Getter boolean isLauncherOutOfDate = false;
    private static @Getter boolean isPlatformSupported = true;

    private static final String launchCommand;

    static {
        appDirectory.mkdirs();

        switch (Platform.osDistribution) {
            case MACOS:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated.app/Contents/MacOS/Casterlabs-Caffeinated";
                REMOTE_ZIP_DOWNLOAD_URL += "macOS-amd64";
                break;

            case LINUX:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated";
                REMOTE_ZIP_DOWNLOAD_URL += "Linux-amd64";
                break;

            case WINDOWS_NT:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated.exe";
                REMOTE_ZIP_DOWNLOAD_URL += "Windows-amd64";
                break;

            default:
                launchCommand = null;
                isPlatformSupported = false;
                break;
        }

        if (NO_JRE_DISTS.contains(Platform.osDistribution)) {
            REMOTE_ZIP_DOWNLOAD_URL += "-nojre";
        }

        REMOTE_ZIP_DOWNLOAD_URL += ".zip";

        try {
            int remoteLauncherVersion = Integer.parseInt(WebUtil.sendHttpRequest(new Request.Builder().url(LAUNCHER_VERSION_URL)).trim());

            isLauncherOutOfDate = VERSION < remoteLauncherVersion;
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    public static void borkInstall() {
        buildInfoFile.delete();
    }

    public static boolean needsUpdate() {
        try {
            // Check for existence of files.
            if (!buildInfoFile.exists()) {
                FastLogger.logStatic("Build was not healthy, forcing redownload.");
                return true;
            }

            JsonObject buildInfo = Rson.DEFAULT.fromJson(FileUtil.readFile(buildInfoFile), JsonObject.class);

            // Check the version.
            String installedChannel = buildInfo.getString("buildChannel");
            if (!installedChannel.equals(CHANNEL)) return true;

            String installedCommit = buildInfo.getString("commit");
            String remoteCommit = WebUtil.sendHttpRequest(new Request.Builder().url(REMOTE_COMMIT_URL)).trim();
            if (!remoteCommit.equals(installedCommit)) return true;

            return false;
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

                updateFile.delete();

                switch (Platform.osDistribution) {
                    case LINUX: {
                        // Make the executable... executable on Linux.
                        String executable = appDirectory.getAbsolutePath() + "/Casterlabs-Caffeinated";

                        new ProcessBuilder()
                            .command(
                                "chmod", "+x", executable
                            )
                            .inheritIO()
                            .start()

                            // Wait for exit.
                            .waitFor();
                        break;
                    }

                    case MACOS: {
                        // Unquarantine the app on MacOS.
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
                        break;
                    }

                    default:
                        break;
                }

                if (NO_JRE_DISTS.contains(Platform.osDistribution)) {
                    // Use the updater's built-in JRE instead of needing to ship one with
                    // Caffeinated. We use the fat build on macOS.

                    // Figure out where the updater's base dir is.
                    String updaterCommandLine = Platform.tryGetCommandLine(); // "C:\Program Files\Casterlabs Caffeinated\Casterlabs-Caffeinated-Updater.exe"
                    updaterCommandLine = updaterCommandLine.substring(1); // Chop off the leading quote.
                    updaterCommandLine = updaterCommandLine.substring(0, updaterCommandLine.indexOf('"')); // Chop off the trailing quote (Safe).

                    File updaterExecutable = new File(updaterCommandLine);
                    File updaterDirectory = updaterExecutable.getParentFile();

                    // Read the manifest file for the newly downloaded app.
                    File manifestFile = new File(appDirectory, "Casterlabs-Caffeinated.json");
                    JsonObject manifest = Rson.DEFAULT.fromJson(FileUtil.readFile(manifestFile), JsonObject.class);

                    // Update said manifest to utilize the updater's JRE.
                    manifest.put("jrePath", new File(updaterDirectory, "jre").getAbsolutePath());
                    Files.write(manifestFile.toPath(), manifest.toString(true).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.DOWNLOAD_FAILED, "Update failed :(", e);
        }
    }

    public static void launch(UpdaterDialog dialog) throws UpdaterException {
        try {
//            if (INLINE_PLATFORMS.contains(Platform.osDistribution)) {
//                // Here's where the fun starts, we load all the jars and run the in the same
//                // process 👀
//
//                // Change directory to the app dir, otherwise the app will pollute the updater's
//                // installation directory and the world would melt.
//                ChangeDir.changeProcessDir(appDirectory);
//
//                // Load and parse the manifest.
//                String manifestContent = FileUtil.readFile(new File(appDirectory, "Casterlabs-Caffeinated.json"))
//                    .replace("\n", "");
//
//                JsonObject manifest = Rson.DEFAULT.fromJson(
//                    manifestContent,
//                    JsonObject.class
//                );
//
//                JsonArray classpathDecl = manifest.getArray("classPath");
//
//                URL[] classpath = new URL[classpathDecl.size()];
//                String mainClassName = manifest.getString("mainClass");
//                String[] args = new String[0];
//
//                for (int idx = 0; idx < classpath.length; idx++) {
//                    classpath[idx] = new File(appDirectory, classpathDecl.getString(idx))
//                        .toURI()
//                        .toURL();
//                }
//
//                // Load the app's classpath but keep it separated from the updater's
//                ClassLoader loader = new URLClassLoader(classpath, ClassLoader.getPlatformClassLoader());
//                Class<?> mainClass = Class.forName(mainClassName, true, loader);
//                Method mainMethod = mainClass.getMethod("main", String[].class);
//
//                // Hide the updater dialog, invoke the main method, and pray.
//                dialog.dispose();
//                mainMethod.invoke(null, (Object) args);
//                return;
//            }

            String updaterCommandLine = Platform.tryGetCommandLine();
            FastLogger.logStatic("Updater CommandLine: %s", updaterCommandLine);
            expectUpdaterFile.createNewFile();
            Files.writeString(expectUpdaterFile.toPath(), updaterCommandLine);

            ProcessBuilder pb = new ProcessBuilder()
                .directory(appDirectory)
                .command(launchCommand, "--started-by-updater");

            // TODO look for the build info file before trusting the process. (kill & let
            // the user know it's dead)

            if (Platform.osDistribution == OSDistribution.MACOS) {
                // On MacOS we do not want to keep the updater process open as it'll stick in
                // the dock. So we start the process and kill the updater to make sure that
                // doesn't happen.
                FastLogger.logStatic(LogLevel.INFO, "The process will now exit, this is so the updater's icon doesn't stick in the dock.");
                pb.start();
                dialog.close();
                System.exit(0);
                return;
            }

            Process proc = pb
                .redirectOutput(Redirect.PIPE)
                .start();

            Scanner in = new Scanner(proc.getInputStream());
            boolean hasAlreadyStarted = false;

            try {
                while (true) {
                    String line = in.nextLine();
                    System.out.println(line);

                    if (!hasAlreadyStarted && line.contains("Starting the UI")) {
                        // Look for "Starting the UI" before we close the dialog.
                        FastLogger.logStatic(LogLevel.INFO, "UI Started!");
                        dialog.close();
                        System.exit(0);
                    }
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.LAUNCH_FAILED, "Could not launch update :(", e);
        }
    }

}

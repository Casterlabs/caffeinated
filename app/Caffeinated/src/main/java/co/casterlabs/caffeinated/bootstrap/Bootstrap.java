package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import co.casterlabs.caffeinated.app.BuildInfo;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.linux.common.LinuxBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.macos.common.MacOSBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.WindowsBootstrap;
import co.casterlabs.caffeinated.localserver.LocalServer;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerWebview.SaucerWebviewListener;
import co.casterlabs.saucer.SaucerWindow.SaucerWindowListener;
import co.casterlabs.saucer.utils.SaucerApp;
import co.casterlabs.saucer.utils.SaucerPreferences;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
@Command(name = "start", mixinStandardHelpOptions = true, version = "Caffeinated", description = "Starts Caffeinated")
public class Bootstrap implements Runnable {

    @Option(names = {
            "-D",
            "--dev-address"
    }, description = "Whether or not this is a dev environment, normal users beware.")
    private String devAddress;

    @Option(names = {
            "-dt",
            "--dev-tools"
    }, description = "Enables dev tools.")
    private boolean devToolsEnabled;

    @Option(names = {
            "-d",
            "--debug"
    }, description = "Enables debug logging.")
    private boolean enableDebugLogging;

    @Option(names = {
            "-t",
            "--trace"
    }, description = "Enables trace logging.")
    private boolean enableTraceLogging;

    @Option(names = {
            "-ec",
            "--enable-color"
    }, description = "Enables colored output.")
    private boolean enableColor = false;

    @Option(names = {
            "--started-by-updater"
    }, description = "Internal use only.")
    private boolean startedByUpdater = false;

    @Deprecated
    @Option(names = {
            "--restart-commandline"
    }, description = "Unused.")
    private String $unused_restartCommandLine;

    private static String restartCommandLine = null;
    private static boolean restartWithConsole = false;

    private static FastLogger logger = new FastLogger();

    private static @Getter Bootstrap instance;
    private static LocalServer localServer;

    private static @Getter BuildInfo buildInfo;

    private static @Getter Saucer saucer;
    private static @Getter boolean isDev;

    public static void main(String[] args) throws Exception {
        Bootstrap.class.getClassLoader().setDefaultAssertionStatus(true);
        SaucerApp.initialize("co.casterlabs.caffeinated", () -> {
            System.out.println(" > System.out.println(\"Hello World!\");\nHello World!\n\n");

            NativeBootstrap nb = null;
            switch (Platform.osDistribution) {
                case LINUX:
                    nb = new LinuxBootstrap();
                    break;

                case MACOS:
                    nb = new MacOSBootstrap();
                    break;

                case WINDOWS_NT:
                    nb = new WindowsBootstrap();
                    break;

                default:
                    break;
            }

            assert nb != null : "Unsupported platform: " + Platform.osDistribution;

            try {
                nb.init();
            } catch (Exception e) {
                e.printStackTrace();
                SaucerApp.quit();
            }

            new CommandLine(new Bootstrap()).execute(args); // Calls #run()
        });
    }

    @SneakyThrows
    @Override
    public void run() {
        FastLoggingFramework.setColorEnabled(this.enableColor);
        System.setProperty("fastloggingframework.wrapsystem", "true");

        File expectUpdaterFile = getAppFile("expect-updater");
        if (expectUpdaterFile.exists()) {
            restartCommandLine = new String(Files.readAllBytes(expectUpdaterFile.toPath()));

            if (this.startedByUpdater) {
                logger.info("App has been started by the updater, cool beans.");
            } else {
                logger.warn("App was not started by the updater and the expect-updater file is present. Launching updater.");
                relaunch();
                return;
            }
        }

        isDev = this.devAddress != null;
        ReflectionLib.setStaticValue(FileUtil.class, "isDev", isDev);
        buildInfo = Rson.DEFAULT.fromJson(FileUtil.loadResource("build_info.json"), BuildInfo.class);

        writeAppFile("current_build_info.json", FileUtil.loadResourceBytes("build_info.json"));

        ReflectionLib.setStaticValue(CaffeinatedPlugin.class, "devEnvironment", isDev);

        // Check for another instance, and do IPC things.
        if (!InstanceManager.isSingleInstance()) {
            if (isDev) {
                logger.info("App is already running, closing it now.");
                InstanceManager.closeOtherInstance();
                logger.info("Launching as if nothing happened...");
            } else {
                logger.info("App is already running, summoning it now.");

                if (InstanceManager.trySummonInstance()) {
                    System.exit(0);
                    return;
                } else {
                    logger.warn("Summon failed, launching anyways.");
                }
            }
        } else {
            logger.info("Starting app.");
        }

        InstanceManager.startIpcHost();

        // We do this down here because of the IPC.
        if (this.enableTraceLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.TRACE);
        } else if (isDev || this.enableDebugLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }

        // Update the log level.
        logger.setCurrentLevel(FastLoggingFramework.getDefaultLevel());

        AsyncTask.create(() -> {
            try {
                this.startApp();
            } catch (Exception e) {
                e.printStackTrace();
                shutdown();
            }
        });
    }

    private static void writeAppFile(@NonNull String filename, byte[] bytes) throws IOException {
        File file = getAppFile(filename);

        if (bytes == null) {
            file.createNewFile();
        } else {
            Files.write(file.toPath(), bytes);
        }
    }

    private static File getAppFile(@NonNull String filename) throws IOException {
        if (Platform.osDistribution == OSDistribution.MACOS) {
            if (new File("./").getCanonicalPath().contains(".app")) {
                return new File("../../../", filename);
            }
        }
        return new File(filename);
    }

    private void startApp() throws Exception {
        CaffeinatedApp app = new CaffeinatedApp(buildInfo, isDev, new NativeSystemImpl());

        logger.info("Entry                        | Value", buildInfo.getVersionString());
        logger.info("-----------------------------+-------------------------");
        logger.info("buildInfo.versionString      | %s", buildInfo.getVersionString());
        logger.info("buildInfo.author             | %s", buildInfo.getAuthor());
        logger.info("buildInfo.isDev              | %b", isDev);
        logger.info("platform.arch                | %s", Platform.archFamily.getArchTarget(Platform.wordSize, Platform.isBigEndian));
        logger.info("platform.osFamily            | %s", Platform.osFamily);
        logger.info("platform.osDistribution      | %s", Platform.osDistribution);
        logger.info("bootstrap.args               | %s", System.getProperty("sun.java.command"));
        logger.info("");

        // Init and start the local server.
        try {
            localServer = new LocalServer(app.getAppPreferences().get().getConductorPort());
            localServer.start();
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start LocalServer (conductor):");
            FastLogger.logException(e);
        }

        // Setup the webview.
        logger.info("Initializing UI (this may take some time)");

        boolean traySupported = TrayHandler.tryCreateTray();

        SaucerApp.dispatch(() -> {
            Saucer.registerCustomScheme("app");
            saucer = Saucer.create(
                SaucerPreferences.create()
                    .hardwareAcceleration(true)
            );
            saucer.window().setTitle("Casterlabs-Caffeinated");
            saucer.bridge().defineObject("Caffeinated", app);

            saucer.webview().setContextMenuAllowed(false);

            String appUrl = (isDev ? this.devAddress : "app://authority") + "/$caffeinated-sdk-root$";
            saucer.webview().setSchemeHandler(new AppSchemeHandler());
            saucer.webview().setUrl(appUrl);
            logger.info("appAddress = %s", appUrl);

            saucer.window().show();
        });

        try {
            ReflectionLib.setValue(CaffeinatedApp.getInstance(), "saucer", saucer);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {}

//        try {
//            ReflectionLib.setValue(CaffeinatedApp.getInstance().getUI(), "uiVisible", true);
//        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {}
//
        TrayHandler.updateShowCheckbox(true);

        saucer.messages().onMessage((arr) -> {
            String type = arr.getString(0);

            if (arr.size() > 1) {
                JsonObject data = arr.getObject(1);
                onBridgeEvent(type, data);
            } else {
                onBridgeEvent(type, JsonObject.EMPTY_OBJECT);
            }
        }, JsonArray.class);

        saucer.webview().setListener(new SaucerWebviewListener() {
            @Override
            public void onTitle(String newTitle) {
                saucer.window().setTitle(newTitle);
            }
        });

        saucer.window().setListener(new SaucerWindowListener() {
            @Override
            public void onClosed() {
                shutdown();
            }

            @Override
            public boolean shouldAvoidClosing() {
                if (app.canCloseUI()) {
                    if (CaffeinatedApp.getInstance().getUI().getPreferences().isCloseToTray() && traySupported) {
                        saucer.window().hide();
                        CaffeinatedApp.getInstance().getUI().navigate("/blank");
                        TrayHandler.updateShowCheckbox(false);
                        return true;
                    } else {
                        shutdown();
                        return false;
                    }
                } else {
                    saucer.window().focus();
                    return true;
                }
            }
        });

        logger.info("Starting app...");
        app.init(traySupported);

        // If all of that succeeds, we write a file to let the updater know that
        // everything's okay.
        writeAppFile(".build_ok", null);
        logger.info("Everything is running and everything is happy :D");
    }

    private void onBridgeEvent(String type, JsonObject data) {
        try {
            switch (type) {
                case "debug:gc": {
                    System.gc();
                    return;
                }

                case "app:reset": {
                    shutdown(true, true, true);
                    return;
                }

                case "app:restart": {
                    shutdown(true, true, false);
                    return;
                }

                case "app:restart_with_console": {
                    restartWithConsole = true;
                    shutdown(true, true, false);
                    return;
                }
            }
        } catch (Throwable t) {
            logger.severe("Uncaught exception whilst processing bridge event:");
            logger.exception(t);
        }
    }

    public static void shutdown() {
        shutdown(false, false, false);
    }

    private static void shutdown(boolean force, boolean relaunch, boolean isReset) {
        if (CaffeinatedApp.getInstance().canCloseUI() || force) {
            logger.info("Shutting down.");

            // Hide the window IMMEDIATELY.
            saucer.window().hide();
            CaffeinatedApp.getInstance().getUI().navigate("/blank");

            // Local Server
            try {
                localServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // UI
            TrayHandler.destroy();
            saucer.close();
            SaucerApp.quit();

            // App
            CaffeinatedApp.getInstance().shutdown();
            InstanceManager.cleanShutdown();

            // Exit.
            if (isReset) {
                try {
                    Files.walk(new File(CaffeinatedApp.APP_DATA_DIR).toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (relaunch) {
                relaunch();
            } else {
                System.exit(0);
            }
        } else {
            saucer.window().focus();
        }
    }

    @SneakyThrows
    private static void relaunch() {
        String command;

        if (restartCommandLine != null) {
            command = restartCommandLine;
        } else {
            String jvmArgs = String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments());
            String entry = System.getProperty("sun.java.command"); // Tested, present in OpenJDK and Oracle
            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");

            String[] args = entry.split(" ");
            File entryFile = new File(args[0]);

            if (entryFile.exists()) { // If the entry is a file, not a main method.
                args[0] = '"' + entryFile.getCanonicalPath() + '"'; // Use raw file path.

                command = String.format("\"%s/bin/java\" %s -cp \"%s\" -jar %s", javaHome, jvmArgs, classpath, String.join(" ", args));
            } else {
                command = String.format("\"%s/bin/java\" %s -cp \"%s\" %s", javaHome, jvmArgs, classpath, entry);
            }
        }

        if (restartWithConsole) {
            ConsoleUtil.startConsoleWindow(command);
        } else {
            Runtime.getRuntime().exec(command);
        }

        FastLogger.logStatic("Relaunching with command: %s", command);
        System.exit(0);
    }

}

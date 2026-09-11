package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import co.casterlabs.caffeinated.app.App;
import co.casterlabs.caffeinated.app.StartupProgress;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.app.util.Resources;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.linux.common.LinuxBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.macos.common.MacOSBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.WindowsBootstrap;
import co.casterlabs.caffeinated.localserver.LocalServer;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.window.AppWindow;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
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
    private static @Getter NativeBootstrap nativeBootstrap;
    private static LocalServer localServer;

    private static @Getter BuildInfo buildInfo;

    private static @Getter boolean isDev;

    private static volatile boolean isShuttingDown = false;

    public static void main(String[] args) throws Exception {
        Bootstrap.class.getClassLoader().setDefaultAssertionStatus(true);

        System.setProperty("saucer.java.help.dependencies", "https://casterlabs.co/caffeinated/dependencies");
        System.setProperty("saucer.generate_typescript_definitions", "true");

        System.out.println(" > System.out.println(\"Hello World!\");\nHello World!\n\n");

        switch (Platform.osDistribution) {
            case LINUX:
                nativeBootstrap = new LinuxBootstrap();
                break;

            case MACOS:
                nativeBootstrap = new MacOSBootstrap();
                break;

            case WINDOWS_NT:
                nativeBootstrap = new WindowsBootstrap();
                break;

            default:
                break;
        }

        assert nativeBootstrap != null : "Unsupported platform: " + Platform.osDistribution;

        try {
            nativeBootstrap.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new CommandLine(new Bootstrap()).execute(args); // Calls #run()
    }

    @SneakyThrows
    @Override
    public void run() {
        System.setProperty("fastloggingframework.wrapsystem", "true");
        FastLoggingFramework.setColorEnabled(this.enableColor);

        {
            Thread gcThread = new Thread(() -> {
                while (true) {
                    System.gc();
                    Thread.yield(); // Willingly yield to the OS if need be.
                    try {
                        Thread.sleep(30_000);
                    } catch (InterruptedException ignored) {}
                }
            });
            gcThread.setDaemon(true);
            gcThread.setName("GC Thread");
            gcThread.start();
        }

        if (this.enableTraceLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.TRACE);
        } else if (isDev || this.enableDebugLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }
        logger.setCurrentLevel(FastLoggingFramework.getDefaultLevel());

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

        Runtime.getRuntime().addShutdownHook(new Thread(Bootstrap::shutdown));

        isDev = this.devAddress != null;
        buildInfo = Rson.DEFAULT.fromJson(Resources.string("build_info.json"), BuildInfo.class);

        writeAppFile("current_build_info.json", Resources.bytes("build_info.json"));

        ReflectionLib.setStaticValue(CaffeinatedPlugin.class, "devEnvironment", isDev);

        new IPCWatcher(new File(AppConfig.APP_DATA_DIR, "/ipc/die")) {
            @Override
            public void onTrigger() {
                shutdown();
            }
        }.start();

        try {
            this.startApp();
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
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
        logger.info("Entry                        | Value");
        logger.info("-----------------------------+-------------------------");
        logger.info("BuildInfo.versionString      | %s", buildInfo.getVersionString());
        logger.info("BuildInfo.author             | %s", buildInfo.getAuthor());
        logger.info("BuildInfo.isDev              | %b", isDev);
        logger.info("Bootstrap.args               | %s", System.getProperty("sun.java.command"));
//        logger.info("SaucerApp.archTarget()       | %s", SaucerApp.archTarget());
//        logger.info("SaucerApp.systemTarget()     | %s", SaucerApp.systemTarget());
//        logger.info("SaucerApp.backendType()      | %s", SaucerApp.backendType());
//        logger.info("SaucerApp.version()          | %s", SaucerApp.version());
        logger.info("");

        StartupProgress.increment("Checking system tray support");
        boolean traySupported = TrayHandler.tryCreateTray();

        StartupProgress.increment("Initializing UI (this may take some time)");
        String appUrl = (isDev ? this.devAddress : "app://authority") + "/$caffeinated-sdk-root$";
        logger.info("appAddress = %s", appUrl);

        AppWindow.INSTANCE.init(
            appUrl,
            traySupported,
            (arr) -> {
                String type = arr.getString(0);

                if (arr.size() > 1) {
                    JsonObject data = arr.getObject(1);
                    onBridgeEvent(type, data);
                } else {
                    onBridgeEvent(type, JsonObject.EMPTY_OBJECT);
                }
            }
        );
        StartupProgress.uiInitialized = true;

        AsyncTask.create(() -> {
            try {
                StartupProgress.increment("Initializing App");
                App.init(buildInfo, isDev, new NativeSystemImpl(), traySupported);

                try {
                    StartupProgress.increment("Initializing LocalServer");
                    FastLogger.logStatic("Initializing LocalServer (conductor) on port %d", AppConfig.appPreferences.get().conductorPort());
                    localServer = new LocalServer(AppConfig.appPreferences.get().conductorPort());
                    localServer.start();
                } catch (Exception e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "Unable to start LocalServer (conductor):");
                    FastLogger.logException(e);
                }

                if (isDev) {
                    AsyncTask.create(() -> {
                        logger.info("Dev tools enabled, opening dev tools.");
                        AppWindow.INSTANCE.openDevTools();
                    });
                }

                // If all of that succeeds, we write a file to let the updater know that
                // everything's okay.
//                SaucerApp.dispatch(() -> {
                try {
                    writeAppFile(".build_ok", null);
                } catch (IOException ignored) {}
                logger.info("Everything is running and everything is happy :D");
//                });
            } catch (Throwable t) {
                logger.severe("Unable to start the app: %s", t);
                shutdown();
            }
        });

        logger.info("Calling run() loop...");
        AppWindow.INSTANCE.run();
        logger.info("run() loop exited.");
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
        if (!App.canCloseUI() && !force) {
            AppWindow.INSTANCE.show();
        }

        if (isShuttingDown) return;
        isShuttingDown = true; // Loop prevention.

        logger.info("Shutting down.");

        // Hide the window IMMEDIATELY.
        AppWindow.INSTANCE.hide();
        AppUI.navigate("/blank");

        // Local Server
        try {
            localServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // App
        App.shutdown();

        // UI
        TrayHandler.destroy();

        // Exit.
        if (isReset) {
            try {
                Files.walk(new File(AppConfig.APP_DATA_DIR).toPath())
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
            ConsoleUtil.startConsoleWindow('"' + command + '"');
        } else {
            Runtime.getRuntime().exec(new String[] {
                    command
            });
        }

        FastLogger.logStatic("Relaunching with command: %s", command);
        System.exit(0);
    }

}

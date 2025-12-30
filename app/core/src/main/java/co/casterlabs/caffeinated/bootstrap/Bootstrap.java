package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import app.saucer.SaucerApp;
import app.saucer.SaucerDesktop;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerNavigation;
import app.saucer.webview.SaucerNavigation.NavigationType;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.SaucerWebviewListener;
import app.saucer.webview.window.SaucerWindow;
import app.saucer.webview.window.SaucerWindowListener;
import co.casterlabs.caffeinated.app.BuildInfo;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.Resources;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.linux.common.LinuxBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.macos.common.MacOSBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.WindowsBootstrap;
import co.casterlabs.caffeinated.localserver.LocalServer;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
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
    private static LocalServer localServer;

    private static @Getter BuildInfo buildInfo;

    private static @Getter SaucerWebview saucer;
    private static @Getter String appUrl;
    private static @Getter boolean isDev;

    private static volatile boolean isShuttingDown = false;

    public static void main(String[] args) throws Exception {
        System.setProperty("saucer.java.help.dependencies", "https://casterlabs.co/caffeinated/dependencies");
        Bootstrap.class.getClassLoader().setDefaultAssertionStatus(true);

        SaucerApp.initialize("co.casterlabs.caffeinated", false);
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
                        Thread.sleep(2000);
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

        new IPCWatcher(new File(CaffeinatedApp.APP_DATA_DIR, "/ipc/die")) {
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

        CaffeinatedApp app = new CaffeinatedApp(buildInfo, isDev, new NativeSystemImpl());

        logger.info("Checking system tray support...");
        boolean traySupported = TrayHandler.tryCreateTray();

        // Init and start the local server.
        try {
            localServer = new LocalServer(app.getAppPreferences().get().getConductorPort());
            localServer.start();
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start LocalServer (conductor):");
            FastLogger.logException(e);
        }

        // Setup the webview.
        SaucerWebview.registerCustomScheme("app");

        SaucerWindow window = SaucerWindow.create();
        saucer = window.createWebview((opts) -> {
            opts.hardwareAcceleration(true);

            switch (SaucerApp.backendType()) {
                case WEBVIEW2:
                    opts.appendBrowserFlag("-msWebView2SimulateMemoryPressureWhenInactive=true");
                    break;
                default:
                    break; // N/A
            }
        });

        logger.info("Starting app...");
        try {
            ReflectionLib.setValue(CaffeinatedApp.getInstance(), "saucer", saucer);
            app.init(traySupported);

            // If all of that succeeds, we write a file to let the updater know that
            // everything's okay.
            writeAppFile(".build_ok", null);
            logger.info("Everything is running and everything is happy :D");
        } catch (Throwable t) {
            logger.severe("Unable to start the app: %s", t);
            shutdown();
        }

        logger.info("Initializing UI (this may take some time)");
        appUrl = (isDev ? this.devAddress : "app://authority") + "/$caffeinated-sdk-root$";
        logger.info("appAddress = %s", appUrl);

        saucer.window.title("Casterlabs-Caffeinated");
        saucer.bridge.defineObject("Caffeinated", app);

        saucer.contextMenuAllowed(false);

        saucer.addSchemeHandler("app", AppSchemeHandler.INSTANCE);
        saucer.url(SaucerUrl.parse(appUrl));

        saucer.window.show();
        TrayHandler.updateShowCheckbox(true);

        saucer.messages.onMessage((arr) -> {
            String type = arr.getString(0);

            if (arr.size() > 1) {
                JsonObject data = arr.getObject(1);
                onBridgeEvent(type, data);
            } else {
                onBridgeEvent(type, JsonObject.EMPTY_OBJECT);
            }
        }, JsonArray.class);

        saucer.listener(new SaucerWebviewListener() {
            @Override
            public boolean onNavigate(SaucerNavigation navigation) {
                if (navigation.type() == NavigationType.NEW_WINDOW) {
                    SaucerDesktop.open(navigation.targetUrl().toString());
                    return false;
                }
                return true;
            }

            @Override
            public void onTitle(String newTitle) {
                if (newTitle.contains("app://") || newTitle.contains("/$caffeinated-sdk-root$")) {
                    newTitle = "Casterlabs-Caffeinated";
                }

                saucer.window.title(newTitle);
            }
        });

        saucer.window.listener(new SaucerWindowListener() {
            @Override
            public void onClosed() {
                shutdown();
            }

            @Override
            public boolean shouldAvoidClosing() {
                if (app.canCloseUI()) {
                    if (CaffeinatedApp.getInstance().getUI().getPreferences().isCloseToTray() && traySupported) {
                        saucer.window.hide();
                        CaffeinatedApp.getInstance().getUI().navigate("/blank");
                        TrayHandler.updateShowCheckbox(false);
                        return true;
                    } else {
                        shutdown();
                        return false;
                    }
                } else {
                    saucer.window.focus();
                    return true;
                }
            }
        });

        logger.info("Calling run() loop...");
        SaucerApp.run();
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
        if (!CaffeinatedApp.getInstance().canCloseUI() && !force) {
            saucer.window.focus();
        }

        if (isShuttingDown) return;
        isShuttingDown = true; // Loop prevention.

        logger.info("Shutting down.");

        // Hide the window IMMEDIATELY.
        saucer.window.hide();
        CaffeinatedApp.getInstance().getUI().navigate("/blank");

        // Local Server
        try {
            localServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // App
        CaffeinatedApp.getInstance().shutdown();

        // UI
        TrayHandler.destroy();
        saucer.window.destroy();
        SaucerApp.quit();

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

package co.casterlabs.caffeinated.window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import co.casterlabs.rakurai.json.element.JsonArray;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@SuppressWarnings("deprecation")
public abstract class AppWindow {
    public static final AppWindow INSTANCE;

    static {
        List<Throwable> errors = new ArrayList<>();

        AppWindow instance = null;

        try {
            Class<?> clazz = Class.forName("co.casterlabs.caffeinated.window.jcef._JcefAppWindow");
            instance = (AppWindow) clazz.newInstance();
            FastLogger.logStatic("Using JCEF for the AppWindow.");
        } catch (Throwable t) {
            FastLogger.logStatic("Failed to load JCEF for the AppWindow. Falling back to Saucer.\n%s", t);
            errors.add(t);
        }

        if (instance == null) {
            try {
                Class<?> clazz = Class.forName("co.casterlabs.caffeinated.window.saucer._SaucerAppWindow");
                instance = (AppWindow) clazz.newInstance();
                FastLogger.logStatic("Using Saucer for the AppWindow.");
            } catch (Throwable t) {
                FastLogger.logStatic("Failed to load Saucer for the AppWindow.\n%s", t);
                errors.add(t);
            }
        }

        if (instance == null) {
            RuntimeException e = new RuntimeException("Failed to initialize AppWindow.");
            errors.forEach(e::addSuppressed);
            throw e;
        }

        INSTANCE = instance;
    }

    public abstract void run();

    public abstract void quit();

    public abstract void open(String url);

    public abstract void init(String appUrl, boolean traySupported, Consumer<JsonArray> messageHandler);

    public abstract boolean isVisible();

    public abstract void show();

    public abstract void hide();

    public abstract void openDevTools();

    public abstract void emit(Object... args);

    public abstract void executeJavaScript(String code);

    public abstract void back();

    public abstract void setIcon(byte[] icon);

    public abstract void forceDarkEnabled(boolean enabled);

    @JavascriptObject
    public static class LogBridge {
        private static final FastLogger LOGGER = new FastLogger("AppWindow");

        @JavascriptFunction
        public static void log(String level, String message) {
            switch (level) {
                case "trace":
                    LOGGER.trace(message);
                    break;
                case "debug":
                    LOGGER.debug(message);
                    break;
                case "info":
                case "log":
                    LOGGER.info(message);
                    break;
                case "warn":
                    LOGGER.warn(message);
                    break;
                case "error":
                    LOGGER.severe(message);
                    break;
            }
        }

    }

}

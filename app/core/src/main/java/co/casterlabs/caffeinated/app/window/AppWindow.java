package co.casterlabs.caffeinated.app.window;

import java.util.function.Consumer;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import co.casterlabs.caffeinated.app.window.saucer._SaucerAppWindow;
import co.casterlabs.rakurai.json.element.JsonArray;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class AppWindow {
    public static final AppWindow INSTANCE = new _SaucerAppWindow();

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

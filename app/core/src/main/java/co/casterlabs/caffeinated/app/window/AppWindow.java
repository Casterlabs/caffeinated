package co.casterlabs.caffeinated.app.window;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import app.saucer.SaucerApp;
import app.saucer.SaucerDesktop;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerNavigation;
import app.saucer.webview.SaucerNavigation.NavigationType;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.SaucerWebviewListener;
import app.saucer.webview.window.SaucerIcon;
import app.saucer.webview.window.SaucerWindow;
import app.saucer.webview.window.SaucerWindowListener;
import co.casterlabs.caffeinated.app.App;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.chatbot.AppChatbot;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.koi.KoiImpl;
import co.casterlabs.caffeinated.app.locale.AppLocale;
import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.app.plugins.AppPlugins;
import co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl;
import co.casterlabs.caffeinated.app.sdk.EmojisImpl;
import co.casterlabs.caffeinated.app.ui.AppThemeManager;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.bootstrap.AppSchemeHandler;
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.TrayHandler;
import co.casterlabs.rakurai.json.element.JsonArray;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppWindow {
    private static @Nullable SaucerWebview saucer;
    private static String appUrl;
    private static boolean traySupported;
    private static Consumer<JsonArray> messageHandler;

    private static SaucerIcon icon;
    private static boolean forceDarkEnabled = false;

    public static void init(String appUrl, boolean traySupported, Consumer<JsonArray> messageHandler) {
        AppWindow.appUrl = appUrl;
        AppWindow.traySupported = traySupported;
        AppWindow.messageHandler = messageHandler;
        show();
    }

    public static synchronized boolean isVisible() {
        return (saucer != null) && saucer.window.isVisible();
    }

    public static synchronized void show() {
        if (saucer != null) {
            saucer.window.show();
            saucer.window.focus();
            return;
        }

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

        if (icon != null) {
            saucer.window.icon(icon);
        }
        saucer.forceDarkEnabled(forceDarkEnabled);

        saucer.messages.onMessage(messageHandler, JsonArray.class);

        saucer.listener(new SaucerWebviewListener() {
            @Override
            public boolean onNavigate(SaucerNavigation navigation) {
                String url = navigation.targetUrl().toString();
                if (navigation.type() == NavigationType.NEW_WINDOW && !url.startsWith("app://")) {
                    SaucerDesktop.open(url);
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
            public void onClosed() {}

            @Override
            public boolean shouldAvoidClosing() {
                if (!App.canCloseUI()) {
                    return true;
                }

                if (!AppConfig.uiPreferences.get().isCloseToTray() || !traySupported) {
                    Bootstrap.shutdown();
                    return false;
                }

                hide();
                return true;
            }
        });

        saucer.window.title("Casterlabs-Caffeinated");
        saucer.contextMenuAllowed(false);
        saucer.addSchemeHandler("app", AppSchemeHandler.INSTANCE);

        // @formatter:off
        saucer.bridge.defineObject("LogBridge", LogBridge.class);

        saucer.bridge.defineObject("App",             App.class);
        saucer.bridge.defineObject("AppAuth",         AppAuth.class);
        saucer.bridge.defineObject("AppChatbot",      AppChatbot.class);
        saucer.bridge.defineObject("AppConfig",       AppConfig.class);
        saucer.bridge.defineObject("AppLocale",       AppLocale.class);
        saucer.bridge.defineObject("AppPlugins",      AppPlugins.class);
        saucer.bridge.defineObject("AppSounds",       AppSounds.class);
        saucer.bridge.defineObject("AppThemeManager", AppThemeManager.class);
        saucer.bridge.defineObject("AppUI",           AppUI.class);

        saucer.bridge.defineObject("Caffeinated", CaffeinatedImpl.INSTANCE);
        saucer.bridge.defineObject("Emojis",      EmojisImpl.INSTANCE);
        saucer.bridge.defineObject("Koi",         KoiImpl.INSTANCE);
        saucer.bridge.defineObject("Music",       MusicImpl.INSTANCE);
        // @formatter:on

        saucer.url(SaucerUrl.parse(appUrl)); // Refresh the URL to ensure it's up to date.
        saucer.window.show();
        saucer.window.focus();

        if (traySupported) TrayHandler.updateShowCheckbox(true);
    }

    public static synchronized void hide() {
        if (saucer == null) return;

        saucer.window.hide();
        saucer.window.destroy();
        saucer = null;

        if (traySupported) TrayHandler.updateShowCheckbox(false);
    }

    public static synchronized void openDevTools() {
        if (saucer != null) {
            saucer.devToolsVisible(true);
        }
    }

    public static synchronized void emit(Object... args) {
        if (saucer != null) {
            saucer.messages.emit(args);
        }
    }

    public static synchronized void executeJavaScript(String code) {
        if (saucer != null) {
            saucer.bridge.executeJavaScript(code);
        }
    }

    public static synchronized void back() {
        if (saucer != null) {
            saucer.back();
        }
    }

    public static synchronized void setIcon(SaucerIcon icon) {
        AppWindow.icon = icon;

        if (saucer != null) {
            saucer.window.icon(icon);
        }
    }

    public static synchronized void forceDarkEnabled(boolean enabled) {
        forceDarkEnabled = enabled;
        if (saucer != null) {
            saucer.forceDarkEnabled(enabled);
        }
    }

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

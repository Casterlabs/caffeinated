package co.casterlabs.caffeinated.app.window.saucer;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import app.saucer.SaucerApp;
import app.saucer.SaucerDesktop;
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
import co.casterlabs.caffeinated.app.window.AppSounds;
import co.casterlabs.caffeinated.app.window.AppWindow;
import co.casterlabs.caffeinated.bootstrap.AppSchemeHandler;
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.TrayHandler;
import co.casterlabs.rakurai.json.element.JsonArray;

public class _SaucerAppWindow extends AppWindow {
    private @Nullable SaucerWebview saucer;
    private String appUrl;
    private boolean traySupported;
    private Consumer<JsonArray> messageHandler;

    private SaucerIcon icon;
    private boolean forceDarkEnabled = false;

    @Override
    public void init(String appUrl, boolean traySupported, Consumer<JsonArray> messageHandler) {
        this.appUrl = appUrl;
        this.traySupported = traySupported;
        this.messageHandler = messageHandler;
        show();
    }

    @Override
    public synchronized boolean isVisible() {
        return (this.saucer != null) && this.saucer.window.isVisible();
    }

    @Override
    public synchronized void show() {
        if (this.saucer != null) {
            this.saucer.window.show();
            this.saucer.window.focus();
            return;
        }

        SaucerWindow window = SaucerWindow.create();
        this.saucer = window.createWebview((opts) -> {
            opts.hardwareAcceleration(true);

            switch (SaucerApp.backendType()) {
                case WEBVIEW2:
                    opts.appendBrowserFlag("-msWebView2SimulateMemoryPressureWhenInactive=true");
                    break;
                default:
                    break; // N/A
            }
        });

        if (this.icon != null) {
            this.saucer.window.icon(this.icon);
        }
        this.saucer.forceDarkEnabled(this.forceDarkEnabled);

        this.saucer.messages.onMessage(this.messageHandler, JsonArray.class);

        this.saucer.listener(new SaucerWebviewListener() {
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

        this.saucer.window.listener(new SaucerWindowListener() {
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

        this.saucer.window.title("Casterlabs-Caffeinated");
        this.saucer.contextMenuAllowed(false);
        this.saucer.addSchemeHandler("app", AppSchemeHandler.INSTANCE);

        // @formatter:off
        this.saucer.bridge.defineObject("LogBridge", LogBridge.class);

        this.saucer.bridge.defineObject("App",             App.class);
        this.saucer.bridge.defineObject("AppAuth",         AppAuth.class);
        this.saucer.bridge.defineObject("AppChatbot",      AppChatbot.class);
        this.saucer.bridge.defineObject("AppConfig",       AppConfig.class);
        this.saucer.bridge.defineObject("AppLocale",       AppLocale.class);
        this.saucer.bridge.defineObject("AppPlugins",      AppPlugins.class);
        this.saucer.bridge.defineObject("AppSounds",       AppSounds.class);
        this.saucer.bridge.defineObject("AppThemeManager", AppThemeManager.class);
        this.saucer.bridge.defineObject("AppUI",           AppUI.class);

        this.saucer.bridge.defineObject("Caffeinated", CaffeinatedImpl.INSTANCE);
        this.saucer.bridge.defineObject("Emojis",      EmojisImpl.INSTANCE);
        this.saucer.bridge.defineObject("Koi",         KoiImpl.INSTANCE);
        this.saucer.bridge.defineObject("Music",       MusicImpl.INSTANCE);
        // @formatter:on

        this.saucer.url(SaucerUrl.parse(appUrl)); // Refresh the URL to ensure it's up to date.
        this.saucer.window.show();
        this.saucer.window.focus();

        if (this.traySupported) TrayHandler.updateShowCheckbox(true);
    }

    @Override
    public synchronized void hide() {
        if (this.saucer == null) return;

        this.saucer.window.hide();
        this.saucer.window.destroy();
        this.saucer = null;

        if (this.traySupported) TrayHandler.updateShowCheckbox(false);
    }

    @Override
    public synchronized void openDevTools() {
        if (this.saucer != null) {
            this.saucer.devToolsVisible(true);
        }
    }

    @Override
    public synchronized void emit(Object... args) {
        if (saucer != null) {
            saucer.messages.emit(args);
        }
    }

    @Override
    public synchronized void executeJavaScript(String code) {
        if (this.saucer != null) {
            this.saucer.bridge.executeJavaScript(code);
        }
    }

    @Override
    public synchronized void back() {
        if (this.saucer != null) {
            this.saucer.back();
        }
    }

    @Override
    public synchronized void setIcon(byte[] iconBytes) {
        SaucerIcon icon = SaucerIcon.from(iconBytes);

        this.icon = icon;

        if (this.saucer != null) {
            this.saucer.window.icon(icon);
        }
    }

    @Override
    public synchronized void forceDarkEnabled(boolean enabled) {
        this.forceDarkEnabled = enabled;
        if (this.saucer != null) {
            this.saucer.forceDarkEnabled(enabled);
        }
    }

}

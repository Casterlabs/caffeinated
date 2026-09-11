package co.casterlabs.caffeinated.window.jcef;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.swing.ImageIcon;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest.TransitionType;

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
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.TrayHandler;
import co.casterlabs.caffeinated.window.AppSounds;
import co.casterlabs.caffeinated.window.AppWindow;
import co.casterlabs.caffeinated.window.WindowState;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.element.JsonArray;
import lombok.SneakyThrows;

/**
 * This is basically a frankenstein of a webview. It uses JCEF, but I've brought
 * in the IPC bridge from Saucer. This is a tempfix until I can get Saucer
 * working properly.
 */
public class _JcefAppWindow extends AppWindow {
    private String appUrl;
    private boolean traySupported;

    private Frame frame;
    private CefClient client;
    private CefMessageRouter router;
    private _JSBridge bridge = new _JSBridge();

    private CefBrowser browser;

    private CompletableFuture<Void> quitFuture = new CompletableFuture<>();

    @Override
    public void run() {
        this.quitFuture.join();
    }

    @Override
    public void quit() {
        this.quitFuture.complete(null);
    }

    @SneakyThrows
    @Override
    public void open(String link) {
        try {
            Desktop
                .getDesktop()
                .browse(URI.create(link));
        } catch (UnsupportedOperationException ignored) {
            // The yucky.
            switch (Platform.osDistribution) {
                case MACOS:
                    Runtime.getRuntime().exec(new String[] {
                            "open",
                            link
                    });
                    break;

                case WINDOWS_NT:
                    Runtime.getRuntime().exec(new String[] {
                            "rundll32",
                            "url.dll,FileProtocolHandler",
                            link
                    });
                    break;

                case LINUX:
                    Runtime.getRuntime().exec(new String[] {
                            "xdg-open",
                            link
                    });
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void init(String appUrl, boolean traySupported, Consumer<JsonArray> messageHandler) {
        this.appUrl = appUrl;
        this.traySupported = traySupported;

        this.frame = new Frame("Casterlabs-Caffeinated");
        this.frame.setBackground(Color.BLACK);

        this.frame.setSize(AppConfig.windowPreferences.get().getWidth(), AppConfig.windowPreferences.get().getHeight());
        this.frame.setLocation(AppConfig.windowPreferences.get().getX(), AppConfig.windowPreferences.get().getY());
        this.frame.setMinimumSize(new Dimension(WindowState.MIN_WIDTH, WindowState.MIN_HEIGHT));

        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!App.canCloseUI()) {
                    return;
                }

                if (!AppConfig.uiPreferences.get().isCloseToTray() || !traySupported) {
                    Bootstrap.shutdown();
                    return;
                }

                hide();
            }
        });

        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isMaximized()) {
                    AppConfig.windowPreferences.get().setWidth(frame.getWidth());
                    AppConfig.windowPreferences.get().setHeight(frame.getHeight());
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (!isMaximized()) {
                    AppConfig.windowPreferences.get().setX(frame.getX());
                    AppConfig.windowPreferences.get().setY(frame.getY());
                }
            }
        });

        this.bridge.messages.onMessage(messageHandler, JsonArray.class);

        // @formatter:off
        this.bridge.defineObject("LogBridge", LogBridge.class);

        this.bridge.defineObject("App",             App.class);
        this.bridge.defineObject("AppAuth",         AppAuth.class);
        this.bridge.defineObject("AppChatbot",      AppChatbot.class);
        this.bridge.defineObject("AppConfig",       AppConfig.class);
        this.bridge.defineObject("AppLocale",       AppLocale.class);
        this.bridge.defineObject("AppPlugins",      AppPlugins.class);
        this.bridge.defineObject("AppSounds",       AppSounds.INSTANCE);
        this.bridge.defineObject("AppThemeManager", AppThemeManager.class);
        this.bridge.defineObject("AppUI",           AppUI.class);

        this.bridge.defineObject("Caffeinated", CaffeinatedImpl.INSTANCE);
        this.bridge.defineObject("Emojis",      EmojisImpl.INSTANCE);
        this.bridge.defineObject("Koi",         KoiImpl.INSTANCE);
        this.bridge.defineObject("Music",       MusicImpl.INSTANCE);
        // @formatter:on

        this.client = _CefUtil.createCefClient();

        this.client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                if (title.isEmpty() || title.equals("null") || title.equals("undefined") || title.contains("app://") || title.contains("/$caffeinated-sdk-root$")) {
                    title = "Casterlabs-Caffeinated";
                }

                frame.setTitle(title);
            }
        });

        this.client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
                open(targetUrl);
                return true;
            }
        });

        this.client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadStart(CefBrowser _browser, CefFrame _frame, TransitionType transitionType) {
                if (browser == _browser && _frame.isMain()) {
                    bridge.onLoadStart(_frame);
                }
            }
        });

        this.router = CefMessageRouter.create();
        this.router.addHandler(this.bridge, true);
        this.client.addMessageRouter(this.router);

        show();
    }

    private boolean isMaximized() {
        return (this.frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
    }

    @Override
    public synchronized boolean isVisible() {
        return this.frame.isVisible();
    }

    @Override
    public synchronized void show() {
        if (this.browser != null) {
            this.frame.setVisible(true);
            this.frame.toFront();
            return;
        }

        this.browser = this.client.createBrowser(this.appUrl, _CefUtil.ENABLE_OSR, false);
        this.frame.add(this.browser.getUIComponent());

        this.frame.setVisible(true);
        this.frame.toFront();

        if (this.traySupported) TrayHandler.updateShowCheckbox(true);
    }

    @Override
    public synchronized void hide() {
        if (this.browser == null) {
            return;
        }

        this.frame.setVisible(false);
        this.frame.removeAll();

        this.browser.close(true);
        this.browser = null;

        if (this.traySupported) TrayHandler.updateShowCheckbox(false);
    }

    @Override
    public synchronized void openDevTools() {
        if (this.browser != null) {
            this.browser.openDevTools();
        }
    }

    @Override
    public synchronized void emit(Object... args) {
        this.bridge.messages.emit(args);
    }

    @Override
    public synchronized void executeJavaScript(String code) {
        if (this.browser != null) {
            this.browser.executeJavaScript(code, "inline", 0);
        }
    }

    @Override
    public synchronized void back() {
        if (this.browser != null) {
            this.browser.goBack();
        }
    }

    @Override
    public synchronized void setIcon(byte[] iconBytes) {
        Image icon = new ImageIcon(iconBytes).getImage();
        this.frame.setIconImage(icon);
    }

    @Override
    public synchronized void forceDarkEnabled(boolean enabled) {
        Bootstrap.getNativeBootstrap().setDarkAppearance(this.frame, enabled);
    }

}

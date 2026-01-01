package co.casterlabs.caffeinated.app.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import app.saucer.webview.window.SaucerIcon;
import co.casterlabs.caffeinated.app.App;
import co.casterlabs.caffeinated.app.AppWindow;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.RealtimeApiListener;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.plugins.AppPlugins;
import co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl;
import co.casterlabs.caffeinated.app.sdk.EmojisImpl;
import co.casterlabs.caffeinated.bootstrap.TrayHandler;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@JavascriptObject
public class AppUI {
    private static final long TOAST_DURATION = 2250; // 2.25s

    private static @Getter boolean uiFinishedLoad = false;

    @Getter
    @JavascriptValue(allowSet = false)
    private static List<String> fonts = FontProvider.listFonts();

    public static void init() {
        updateEmojiProvider();
    }

    private static void updateEmojiProvider() {
        EmojisImpl.setEmojiProvider(AppConfig.uiPreferences.get().getEmojiProvider());
    }

    public static JsonObject constructSDKPreferences() {
        return new JsonObject()
            .put("emojiProvider", AppConfig.uiPreferences.get().getEmojiProvider())
            .put("language", AppConfig.uiPreferences.get().getLanguage())
            .put("appearance", AppThemeManager.getEffectiveAppearance().name())
            .put("theme", JsonArray.of(AppConfig.themePreferences.get().getBaseColor(), AppConfig.themePreferences.get().getPrimaryColor()))
            .put("zoom", AppConfig.uiPreferences.get().getZoom());
    }

    public static void onUpdatePreferences() {
        App.reloadLanguage();
        updateEmojiProvider();

        JsonObject preferences = constructSDKPreferences();

        // Broadcast to the plugins.
        AsyncTask.create(() -> {
            try {
                // Send the events to the widget instances.
                for (CaffeinatedPlugin plugin : AppPlugins.getLoadedPlugins()) {
                    for (Widget widget : plugin.getWidgets()) {
                        for (WidgetInstance instance : widget.getWidgetInstances()) {
                            try {
                                instance.onAppearanceUpdate(preferences);
                            } catch (IOException ignored) {}
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Broadcast to the local api.
        AsyncTask.create(() -> {
            try {
                // Send the events to the widget instances.
                for (RealtimeApiListener listener : App.apiListeners.toArray(new RealtimeApiListener[0])) {
                    listener.onAppearanceUpdate(preferences);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        updateIcon();
    }

    @JavascriptFunction
    public static void updateDashboard(@NonNull DashboardConfig config, boolean isMain) {
        if (isMain) {
            AppConfig.uiPreferences.get().setMainDashboard(config);
        } else {
            AppConfig.uiPreferences.get().setDockDashboard(config);
        }

        AppConfig.uiPreferences.save();
    }

    @JavascriptFunction
    public static void onUILoaded() {
        uiFinishedLoad = true;

        if (AppConfig.canDoOneTimeEvent("caffeinated.instance.first_time_setup")) {
//            navigate("/welcome/step1");
//            FastLogger.logStatic(LogLevel.DEBUG, "Waiting for first time experience. (ui-loaded)");
//            return;
        }

        if (!AppAuth.isSignedIn()) {
            navigate("/signin");
        } else if (AppAuth.isAuthorized()) {
            navigate("/dashboard");
        } else {
            // Otherwise AppAuth will automagically move us there :D
            FastLogger.logStatic(LogLevel.DEBUG, "Waiting for auth to navigate us. (ui-loaded)");
        }
    }

    /**
     * @deprecated This is not to be used by the app unless it's for very good
     *             reasons. Use
     *             {@link App#notify(String, co.casterlabs.caffeinated.app.NativeSystem.NotificationType)}
     *             instead.
     */
    @Deprecated
    @JavascriptFunction
    public static void showToast(@NonNull String message, @NonNull NotificationType type) {
        if (uiFinishedLoad) {
            String line = String.format(
                "Toastify(%s).showToast();",

                // Build the toastify options.
                new JsonObject()
                    .put("text", CaffeinatedImpl.INSTANCE.localize(message, Collections.emptyMap(), Collections.emptyList()))
                    .put("duration", TOAST_DURATION)
                    .put("close", true)
                    .put(
                        "style", new JsonObject()
                            .put("background", type.getColor())
                    )
            );

            AppWindow.executeJavaScript(line);
        }
    }

    public static void goBack() {
        if (uiFinishedLoad && AppWindow.isVisible()) {
            AppWindow.back();
        }
    }

    public static void navigate(String path) {
        if (uiFinishedLoad) {
            AppWindow.emit(
                "goto",
                JsonObject.singleton("path", "/$caffeinated-sdk-root$" + path)
            );
        }
    }

    @SneakyThrows
    public static void updateIcon() {
        URL resource;

        if (App.isDev) {
            resource = new File("./src/main/resources/assets/logo/hardhat.png").toURI().toURL();
        } else {
            String path;
            if (AppConfig.uiPreferences.get() == null || AppConfig.uiPreferences.get().getIcon() == null) {
                path = "assets/logo/casterlabs.png";
            } else {
                path = String.format("assets/logo/%s.png", AppConfig.uiPreferences.get().getIcon());
            }
            resource = AppUI.class.getClassLoader().getResource(path);
        }

        TrayHandler.changeTrayIcon(ImageIO.read(resource));

        SaucerIcon icon = SaucerIcon.from(StreamUtil.toBytes(resource.openStream()));
        AppWindow.setIcon(icon);
    }

}

package co.casterlabs.caffeinated.app.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.EmojisObj;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.ui.UIPreferences.ActivityViewerPreferences;
import co.casterlabs.caffeinated.app.ui.UIPreferences.ChatViewerPreferences;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptGetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppUI extends JavascriptObject {
    private static final long TOAST_DURATION = 2250; // 2.25s

    private PreferenceFile<UIPreferences> preferenceFile = new PreferenceFile<>("ui", UIPreferences.class);

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private UIPreferences preferences = this.preferenceFile.get();

    private @Getter boolean uiFinishedLoad = false;

    @Getter
    @JavascriptValue(allowSet = false)
    private List<String> fonts = FontProvider.listFonts();

    public void init() {
        this.updateEmojiProvider();
    }

    private void updateEmojiProvider() {
        EmojisObj.setEmojiProvider(this.preferences.getEmojiProvider());
    }

    @JavascriptGetter("chatPreferences")
    public ChatViewerPreferences getChatPreferences() {
        return this.preferences.getChatViewerPreferences();
    }

    @JavascriptSetter("chatPreferences")
    public void updateChatPreferences(@NonNull ChatViewerPreferences prefs) {
        this.preferences.setChatViewerPreferences(prefs);
        this.preferenceFile.save();
    }

    @JavascriptGetter("activityPreferences")
    public ActivityViewerPreferences getActivityPreferences() {
        return this.preferences.getActivityViewerPreferences();
    }

    @JavascriptSetter("activityPreferences")
    public void updateActivityPreferences(@NonNull ActivityViewerPreferences prefs) {
        this.preferences.setActivityViewerPreferences(prefs);
        this.preferenceFile.save();
    }

    @JavascriptFunction
    public void updateAppearance(@NonNull UIPreferences newPreferences) {
        this.preferences.setIcon(newPreferences.getIcon());
        this.preferences.setCloseToTray(newPreferences.isCloseToTray());
        this.preferences.setMikeysMode(newPreferences.isMikeysMode());
        this.preferences.setEmojiProvider(newPreferences.getEmojiProvider());
        this.preferences.setLanguage(newPreferences.getLanguage());
        this.preferences.setEnableStupidlyUnsafeSettings(newPreferences.isEnableStupidlyUnsafeSettings());
        this.preferences.setEnableAlternateThemes(newPreferences.isEnableAlternateThemes());
        this.preferences.setZoom(newPreferences.getZoom());
        this.preferenceFile.save();

        this.updateEmojiProvider();

        // Broadcast to the plugins.
        AsyncTask.create(() -> {
            try {
                JsonObject preferences = (JsonObject) Rson.DEFAULT.toJson(this.preferences);

                // Send the events to the widget instances.
                for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPlugins().getLoadedPlugins()) {
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

        this.updateIcon();
    }

    @JavascriptFunction
    public void updateDashboard(@NonNull DashboardConfig config, boolean isMain) {
        if (isMain) {
            this.preferences.setMainDashboard(config);
        } else {
            this.preferences.setDockDashboard(config);
        }

        this.preferenceFile.save();
    }

    @JavascriptFunction
    public void onUILoaded() {
        this.uiFinishedLoad = true;

//        PreferenceFile<AppPreferences> prefs = CaffeinatedApp.getInstance().getAppPreferences();

//        if (prefs.get().isNew()) {
//            CaffeinatedApp.getInstance().getAnalytics().track("FRESH_INSTALL", true);
//
//            prefs.get().setNew(false);
//            prefs.save();
//
//            this.navigate("/welcome/step1");
//        } else {
        AppAuth auth = CaffeinatedApp.getInstance().getAuth();

        if (!auth.isSignedIn()) {
            this.navigate("/signin");
        } else if (auth.isAuthorized()) {
            this.navigate("/dashboard");
        } else {
            // Otherwise AppAuth will automagically move us there :D
            FastLogger.logStatic(LogLevel.DEBUG, "Waiting for auth to navigate us. (ui-loaded)");
        }
//        }
    }

    @JavascriptFunction
    @SneakyThrows
    public void openLink(@NonNull String link) {
        Desktop
            .getDesktop()
            .browse(new URI(link));
    }

    /**
     * @deprecated This is not to be used by the app unless it's for very good
     *             reasons. Use
     *             {@link CaffeinatedApp#notify(String, co.casterlabs.caffeinated.app.NativeSystem.NotificationType)}
     *             instead.
     */
    @Deprecated
    @JavascriptFunction
    public void showToast(@NonNull String message, @NonNull NotificationType type) {
        if (this.uiFinishedLoad) {
            String line = String.format(
                "Toastify(%s).showToast();",

                // Build the toastify options.
                new JsonObject()
                    .put("text", message)
                    .put("duration", TOAST_DURATION)
                    .put("close", true)
                    .put(
                        "style", new JsonObject()
                            .put("background", type.getColor())
                    )
            );

            CaffeinatedApp.getInstance().getAppBridge().eval(line);
        }
    }

    public void goBack() {
        if (this.uiFinishedLoad) {
            CaffeinatedApp.getInstance().getAppBridge().eval("history.back()");
        }
    }

    public void navigate(String path) {
        if (this.uiFinishedLoad) {
            CaffeinatedApp.getInstance().getAppBridge().emit("goto", JsonObject.singleton("path", "/$caffeinated-sdk-root$" + path));
        }
    }

    @SneakyThrows
    public void updateIcon() {
        URL resource;

        if (CaffeinatedApp.getInstance().isDev()) {
            resource = new File("./src/main/resources/assets/logo/hardhat.png").toURI().toURL();
        } else {
            String path = String.format("assets/logo/%s.png", this.getPreferences().getIcon());
            resource = AppUI.class.getClassLoader().getResource(path);
        }

        App.setIcon(resource);
    }

}

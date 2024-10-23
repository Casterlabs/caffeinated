package co.casterlabs.caffeinated.app.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.EmojisObj;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.RealtimeApiListener;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.ui.UIPreferences.ActivityViewerPreferences;
import co.casterlabs.caffeinated.app.ui.UIPreferences.ChatViewerPreferences;
import co.casterlabs.caffeinated.bootstrap.TrayHandler;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonNumber;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptSetter;
import co.casterlabs.saucer.bridge.JavascriptValue;
import co.casterlabs.saucer.utils.SaucerDesktop;
import co.casterlabs.saucer.utils.SaucerIcon;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@JavascriptObject
public class AppUI {
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

    public JsonObject constructSDKPreferences() {
        return new JsonObject()
            .put("emojiProvider", this.preferences.getEmojiProvider())
            .put("language", this.preferences.getLanguage())
            .put("appearance", CaffeinatedApp.getInstance().getThemeManager().getEffectiveAppearance().name())
            .put("theme", JsonArray.of(CaffeinatedApp.getInstance().getThemeManager().getBaseColor(), CaffeinatedApp.getInstance().getThemeManager().getPrimaryColor()))
            .put("zoom", this.preferences.getZoom());
    }

    @JavascriptFunction
    public void updateAppearance(@NonNull UIPreferences newPreferences) {
        this.preferences.setIcon(newPreferences.getIcon());
        this.preferences.setCloseToTray(newPreferences.isCloseToTray());
        this.preferences.setEmojiProvider(newPreferences.getEmojiProvider());
        this.preferences.setLanguage(newPreferences.getLanguage());
        this.preferences.setEnableStupidlyUnsafeSettings(newPreferences.isEnableStupidlyUnsafeSettings());
        this.preferences.setEnableAlternateThemes(newPreferences.isEnableAlternateThemes());
        this.preferences.setZoom(newPreferences.getZoom());
        this.preferences.setUiFont(newPreferences.getUiFont());
        this.preferences.setSidebarClosed(newPreferences.isSidebarClosed());
        this.preferenceFile.save();

        CaffeinatedApp.getInstance().reloadLanguage();
        this.updateEmojiProvider();

        JsonObject preferences = this.constructSDKPreferences();

        // Broadcast to the plugins.
        AsyncTask.create(() -> {
            try {
                // Send the events to the widget instances.
                for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPluginIntegration().getLoadedPlugins()) {
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
                for (RealtimeApiListener listener : CaffeinatedApp.getInstance().getApiListeners().toArray(new RealtimeApiListener[0])) {
                    listener.onAppearanceUpdate(preferences);
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

        if (CaffeinatedApp.getInstance().canDoOneTimeEvent("caffeinated.instance.first_time_setup")) {
            CaffeinatedApp.getInstance().track("FRESH_INSTALL", true);
//            this.navigate("/welcome/step1");
//            FastLogger.logStatic(LogLevel.DEBUG, "Waiting for first time experience. (ui-loaded)");
//            return;
        }

        AppAuth auth = CaffeinatedApp.getInstance().getAuth();

        if (!auth.isSignedIn()) {
            this.navigate("/signin");
        } else if (auth.isAuthorized()) {
            this.navigate("/dashboard");
        } else {
            // Otherwise AppAuth will automagically move us there :D
            FastLogger.logStatic(LogLevel.DEBUG, "Waiting for auth to navigate us. (ui-loaded)");
        }
    }

    @JavascriptFunction
    @SneakyThrows
    public void openLink(@NonNull String link) {
        if (link.startsWith("#")) return; // Not a real link.

        SaucerDesktop.open(link);
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
                    .put("text", CaffeinatedApp.getInstance().localize(message, Collections.emptyMap(), Collections.emptyList()))
                    .put("duration", TOAST_DURATION)
                    .put("close", true)
                    .put(
                        "style", new JsonObject()
                            .put("background", type.getColor())
                    )
            );

            CaffeinatedApp.getInstance().getSaucer().bridge().executeJavaScript(line);
        }
    }

    public void goBack() {
        if (this.uiFinishedLoad) {
            CaffeinatedApp.getInstance().getSaucer().webview().back();
        }
    }

    public void navigate(String path) {
        if (this.uiFinishedLoad) {
            CaffeinatedApp.getInstance().getSaucer().messages().emit(new Object[] {
                    "goto",
                    JsonObject.singleton("path", "/$caffeinated-sdk-root$" + path)
            });
        }
    }

    public void playAudio(@NonNull String audioUrl, float volume) {
//        if (!this.uiVisible) {
//            CaffeinatedApp.getInstance().notify(
//                "co.casterlabs.caffeinated.app.cannot_play_sounds_without_ui",
//                Collections.emptyMap(),
//                NotificationType.WARNING
//            );
//            return;
//        }

        CaffeinatedApp.getInstance().getSaucer().bridge().executeJavaScript(
            "(() => {"
                + "let previousAudioPromise = window.currentAudioPromise;"
                + "window.currentAudioPromise = new Promise(async (resolve) => {"
                + "  if (previousAudioPromise) await previousAudioPromise;"
                + "  const audio = new Audio(" + new JsonString(audioUrl) + ");"
                + "  audio.addEventListener('ended', resolve);"
                + "  audio.addEventListener('error', resolve);"
                + "  audio.volume = " + new JsonNumber(volume) + ";"
                + "  audio.play();"
                + "});"
                + "})();"
        );
    }

    @SneakyThrows
    public void updateIcon() {
        URL resource;

        if (CaffeinatedApp.getInstance().isDev()) {
            resource = new File("./src/main/resources/assets/logo/hardhat.png").toURI().toURL();
        } else {
            String path;
            if (this.getPreferences() == null || this.getPreferences().getIcon() == null) {
                path = "assets/logo/casterlabs.png";
            } else {
                path = String.format("assets/logo/%s.png", this.getPreferences().getIcon());
            }
            resource = AppUI.class.getClassLoader().getResource(path);
        }

        TrayHandler.changeTrayIcon(ImageIO.read(resource));

        SaucerStash stash = SaucerStash.of(StreamUtil.toBytes(resource.openStream()));
        SaucerIcon icon = SaucerIcon.of(stash);

        CaffeinatedApp.getInstance().getSaucer().window().setIcon(icon);
    }

}

package co.casterlabs.caffeinated.app;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import co.casterlabs.caffeinated.app.api.AppApi;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.auth.AuthPreferences;
import co.casterlabs.caffeinated.app.chatbot.AppChatbot;
import co.casterlabs.caffeinated.app.koi.GlobalKoi;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegrationPreferences;
import co.casterlabs.caffeinated.app.plugins.PluginIntegration;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.theming.ThemeManager;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.kaimen.webview.Webview;
import co.casterlabs.kaimen.webview.bridge.BridgeValue;
import co.casterlabs.kaimen.webview.bridge.WebviewBridge;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

@Getter
public class CaffeinatedApp extends Caffeinated {
    public static final String caffeinatedClientId = "LmHG2ux992BxqQ7w9RJrfhkW";
    public static final String appDataDir;

    // I chose JsonObject because of the builder syntax.
    public static final JsonObject AUTH_URLS = new JsonObject()
        .put("caffeinated_spotify", "https://casterlabs.co/auth/redirect/spotify")
        .put("caffeinated_twitch", "https://casterlabs.co/auth/redirect/twitch")
        .put("caffeinated_trovo", "https://casterlabs.co/auth/redirect/trovo")
        .put("caffeinated_glimesh", "https://casterlabs.co/auth/redirect/glimesh")
        .put("caffeinated_brime", "https://casterlabs.co/auth/redirect/brime");

    private static @Getter CaffeinatedApp instance;

    private final BuildInfo buildInfo;
    private final boolean isDev;

    private @Setter WebviewBridge appBridge;
    private @Setter Webview webview;
    private @Setter String appUrl;

    // @formatter:off
    private PluginIntegration plugins = new PluginIntegration();
    private MusicIntegration  music   = new MusicIntegration();
    private AppChatbot        chatbot = new AppChatbot();
    private GlobalKoi         koi     = new GlobalKoi();
    private AppAuth           auth    = new AppAuth();
    private AppApi            api     = new AppApi();
    private AppUI             UI      = new AppUI();

    private PreferenceFile<PluginIntegrationPreferences> pluginIntegrationPreferences = new PreferenceFile<>("plugins", PluginIntegrationPreferences.class);
    private PreferenceFile<MusicIntegrationPreferences>  musicIntegrationPreferences  = new PreferenceFile<>("music",   MusicIntegrationPreferences.class);
    private PreferenceFile<AppWindowState>               windowPreferences            = new PreferenceFile<>("window",  AppWindowState.class);
    private PreferenceFile<AuthPreferences>              authPreferences              = new PreferenceFile<>("auth",    AuthPreferences.class);
    private PreferenceFile<AppPreferences>               appPreferences               = new PreferenceFile<>("app",     AppPreferences.class).bridge();
    // @formatter:on

    private Map<String, List<Consumer<JsonObject>>> bridgeEventListeners = new HashMap<>();
    private Map<String, List<Consumer<JsonObject>>> appEventListeners = new HashMap<>();

    private static BridgeValue<AppPreferences> bridge_AppPreferences = new BridgeValue<>("app:preferences");

    static {
        AppDirs appDirs = AppDirsFactory.getInstance();
        appDataDir = appDirs.getUserDataDir("casterlabs-caffeinated", null, null, true);

        new File(appDataDir, "preferences").mkdirs();
    }

    public CaffeinatedApp(@NonNull BuildInfo buildInfo, boolean isDev) {
        this.buildInfo = buildInfo;
        this.isDev = isDev;
        instance = this;

        this.UI.updateIcon();
    }

    public void init() {
        new AsyncTask(() -> {
            ThemeManager.init();

            this.UI.init();
            this.api.init();
            this.koi.init();
            this.auth.init();
            this.music.init();
            this.plugins.init();

            this.appBridge.attachValue(bridge_AppPreferences);
            bridge_AppPreferences.set(this.appPreferences.get());
            this.appPreferences.addSaveListener((pref) -> {
                bridge_AppPreferences.update();
            });

            // This doesn't updating, so we register it and leave it be.
            new BridgeValue<BuildInfo>("build").set(this.buildInfo).attachGlobally();

            this.appPreferences.save();
        });
    }

    public boolean canCloseUI() {
        // We can prevent ui closure if needed.
        // Maybe during plugin installs?
        // TODO
        return true;
    }

    public void shutdown() {
        this.auth.shutdown();
    }

    /**
     * Word of caution, you're not supposed to be able to unsubscribe to an event.
     * You have been warned.
     * 
     * If u throw err, i kil.
     */
    public void onBridgeEvent(@NonNull String type, @NonNull Consumer<JsonObject> handler) {
        if (!this.bridgeEventListeners.containsKey(type)) {
            this.bridgeEventListeners.put(type, new LinkedList<>());
        }

        this.bridgeEventListeners.get(type).add(handler);
    }

    /**
     * Word of caution, you're not supposed to be able to unsubscribe to an event.
     * You have been warned.
     * 
     * If u throw err, i kil.
     */
    public void onAppEvent(@NonNull String type, @NonNull Consumer<JsonObject> handler) {
        if (!this.appEventListeners.containsKey(type)) {
            this.appEventListeners.put(type, new LinkedList<>());
        }

        this.appEventListeners.get(type).add(handler);
    }

    public void emitAppEvent(@NonNull String type, @NonNull JsonObject data) {
        if (this.appEventListeners.containsKey(type)) {
            this.appEventListeners
                .get(type)
                .forEach((c) -> c.accept(data));
        }
    }

    @SneakyThrows
    public void onBridgeEvent(String type, JsonObject data) {
        String[] signal = type.split(":", 2);
        String nestedType = signal[1].replace('-', '_').toUpperCase();

        if (this.bridgeEventListeners.containsKey(type)) {
            this.bridgeEventListeners
                .get(type)
                .forEach((c) -> c.accept(data));
        }

        switch (signal[0].toLowerCase()) {

            case "ui": {
                AppUI.invokeEvent(data, nestedType);
                return;
            }

            case "auth": {
                AppAuth.invokeEvent(data, nestedType);
                return;
            }

            case "music": {
                MusicIntegration.invokeEvent(data, nestedType);
                return;
            }

            case "plugins": {
                PluginIntegration.invokeEvent(data, nestedType);
                return;
            }

            case "koi": {
                GlobalKoi.invokeEvent(data, nestedType);
                return;
            }

            case "chatbot": {
                AppChatbot.invokeEvent(data, nestedType);
                return;
            }

        }
    }

}

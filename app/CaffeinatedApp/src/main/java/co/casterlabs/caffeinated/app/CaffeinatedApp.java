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
import co.casterlabs.kaimen.webview.WebviewWindowState;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.kaimen.webview.bridge.WebviewBridge;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

@Getter
public class CaffeinatedApp extends JavascriptObject implements Caffeinated {
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

    private final @JavascriptValue BuildInfo buildInfo;
    private final @JavascriptValue boolean isDev;

    private @Setter WebviewBridge appBridge;
    private @Setter Webview webview;
    private @Setter String appUrl;

    // Integrations
    private PluginIntegration plugins = new PluginIntegration();
    private MusicIntegration music = new MusicIntegration();
    private ThemeManager themeManager = new ThemeManager();
    private AppChatbot chatbot = new AppChatbot();
    private GlobalKoi koi = new GlobalKoi();
    private AppAuth auth = new AppAuth();
    private AppApi api = new AppApi();
    private AppUI UI = new AppUI();

    // Preferences
    private PreferenceFile<PluginIntegrationPreferences> pluginIntegrationPreferences = new PreferenceFile<>("plugins", PluginIntegrationPreferences.class);
    private PreferenceFile<MusicIntegrationPreferences> musicIntegrationPreferences = new PreferenceFile<>("music", MusicIntegrationPreferences.class);
    private PreferenceFile<WebviewWindowState> windowPreferences = new PreferenceFile<>("window", WebviewWindowState.class);
    private PreferenceFile<AuthPreferences> authPreferences = new PreferenceFile<>("auth", AuthPreferences.class);

    @JavascriptValue(allowSet = false)
    private PreferenceFile<AppPreferences> appPreferences = new PreferenceFile<>("app", AppPreferences.class);

    // Event stuff
    private Map<String, List<Consumer<JsonObject>>> appEventListeners = new HashMap<>();

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
            this.themeManager.init();
            this.UI.init();
            this.api.init();
//            this.koi.init();
            this.auth.init();
            this.music.init();
            this.plugins.init();

            this.appBridge.defineObject("Caffeinated", this);

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
    @Deprecated
    public void onAppEvent(@NonNull String type, @NonNull Consumer<JsonObject> handler) {
        if (!this.appEventListeners.containsKey(type)) {
            this.appEventListeners.put(type, new LinkedList<>());
        }

        this.appEventListeners.get(type).add(handler);
    }

    @Deprecated
    public void emitAppEvent(@NonNull String type, @NonNull JsonObject data) {
        if (this.appEventListeners.containsKey(type)) {
            this.appEventListeners
                .get(type)
                .forEach((c) -> c.accept(data));
        }
    }

}

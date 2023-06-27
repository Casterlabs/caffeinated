package co.casterlabs.caffeinated.app;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.api.AppApi;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.auth.AuthPreferences;
import co.casterlabs.caffeinated.app.chatbot.AppChatbot;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences;
import co.casterlabs.caffeinated.app.chatbot.ChatbotScriptEngine;
import co.casterlabs.caffeinated.app.controldeck.AppControlDeck;
import co.casterlabs.caffeinated.app.controldeck.ControlDeckPreferences;
import co.casterlabs.caffeinated.app.koi.GlobalKoi;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.plugins.PluginIntegration;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.app.ui.CaffeinatedWindowState;
import co.casterlabs.caffeinated.app.ui.ThemeManager;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CasterlabsAccount;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.util.ClipboardUtil;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.kaimen.webview.Webview;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptGetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.kaimen.webview.bridge.WebviewBridge;
import co.casterlabs.rakurai.io.http.MimeTypes;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.swetrix.Swetrix;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.loggerimpl.FileLogHandler;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
public class CaffeinatedApp extends JavascriptObject implements Caffeinated {
    private static final String ANALYTICS_ID = "uC69hShzxhbQ";

    public static final String caffeinatedClientId = "LmHG2ux992BxqQ7w9RJrfhkW";
    public static final String appDataDir;

    public static final String AUTH_URL = "https://casterlabs.co/auth/redirect";
    public static final Map<String, String> OVERRIDE_AUTH_URLS = Map.of(
        "spotify", "https://api.casterlabs.co/v2/caffeinated/spotify/auth/redirect",
        "streamlabs", "https://api.casterlabs.co/v2/caffeinated/streamlabs/auth/redirect"
    );

    private static @Getter CaffeinatedApp instance;

    private final @JavascriptValue(allowSet = false) BuildInfo buildInfo;
    private final @JavascriptValue(allowSet = false) boolean isDev;

    private @Setter WebviewBridge appBridge;
    private @Setter Webview webview;
    private @Setter String appUrl;

    private @JavascriptValue(allowSet = false) boolean isTraySupported;

    private Swetrix analytics;

    private Connection preferencesConnection;

    // @formatter:off

    // Integrations
    private PluginIntegration  plugins            = new PluginIntegration();
    private MusicIntegration   music              = new MusicIntegration();
    private AppControlDeck     controlDeck        = new AppControlDeck();
    private ThemeManager       themeManager       = new ThemeManager();
    private AppChatbot         chatbot            = new AppChatbot();
    private GlobalKoi          koi                = new GlobalKoi();
    private AppAuth            auth               = new AppAuth();
    private EmojisObj          emojis             = new EmojisObj();
    private AppApi             api                = new AppApi();
    private AppUI              UI                 = new AppUI();

    // Preferences
    private PreferenceFile<ControlDeckPreferences>       controlDeckPreferences = new PreferenceFile<>("controldeck", ControlDeckPreferences.class);
    private PreferenceFile<ChatbotPreferences>           chatbotPreferences = new PreferenceFile<>("chatbot", ChatbotPreferences.class);
    private PreferenceFile<CaffeinatedWindowState>       windowPreferences = new PreferenceFile<>("window", CaffeinatedWindowState.class);
    private PreferenceFile<AuthPreferences>              authPreferences = new PreferenceFile<>("auth", AuthPreferences.class);

    // @formatter:on

    @JavascriptValue(allowSet = false)
    private PreferenceFile<AppPreferences> appPreferences = new PreferenceFile<>("app", AppPreferences.class);

    // Event stuff
    private Map<String, List<Consumer<JsonObject>>> appEventListeners = new HashMap<>();

    private final NativeSystem nativeSystem;

    static {
        AppDirs appDirs = AppDirsFactory.getInstance();
        appDataDir = appDirs.getUserDataDir("casterlabs-caffeinated", null, null, true);

        new File(appDataDir, "preferences").mkdirs();
        new File(appDataDir, "preferences/old").mkdir();

        final File logsDir = new File(appDataDir, "logs");
        final File logFile = new File(logsDir, "app.log");

        try {
            logsDir.mkdirs();
            logFile.createNewFile();

            new FileLogHandler(logFile);

            FastLogger.logStatic("\n\n---------- %s ----------\n", Instant.now());
            FastLogger.logStatic("Log file: %s", logFile);
        } catch (IOException e) {
            FastLogger.logException(e);
        }
    }

    public CaffeinatedApp(@NonNull BuildInfo buildInfo, boolean isDev, NativeSystem nativeHelper) {
        this.buildInfo = buildInfo;
        this.isDev = isDev;
        this.nativeSystem = nativeHelper;
        instance = this;

        Currencies.getCurrencies(); // Load the class.

        this.UI.updateIcon();

        this.analytics = Swetrix.builder(ANALYTICS_ID)
            .withDebugEnabled(isDev)
            .withAnalyticsDisabled(isDev)
            .build();
    }

    @SneakyThrows
    public void init(boolean traySupported) {
        this.isTraySupported = traySupported;

        this.preferencesConnection = DriverManager.getConnection("jdbc:sqlite:" + new File(CaffeinatedApp.appDataDir, "preferences/database.sqlite").getCanonicalPath());

        this.chatbot.init();
        this.UI.init();
        this.themeManager.init();
        this.auth.init();
        this.controlDeck.init();
        this.api.init();
        this.plugins.init();
//            this.koi.init();
        this.music.init();

        this.appPreferences.save();

        this.analytics.trackPageView("/", this.UI.getPreferences().getLanguage());
        this.analytics.startHeartbeat();

        ChatbotScriptEngine.class.toString(); // Load.
    }

    @Override
    public String getLocale() {
        return this.UI.getPreferences().getLanguage();
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

    @Override
    public void openLink(String url) {
        this.UI.openLink(url);
    }

    @Override
    public String getMimeForPath(String path) {
        return MimeTypes.getMimeForFile(new File(path));
    }

    @SuppressWarnings("deprecation")
    @JavascriptFunction
    @Override
    public void copyText(@NonNull String text, @Nullable String toastText) {
        ClipboardUtil.copy(text);

        if (toastText != null) {
            this.UI.showToast(toastText, NotificationType.NONE);
        }
    }

    /**
     * Sends a system notification, if that fails then it'll fallback on a UI-based
     * notification instead.
     */
    @SuppressWarnings("deprecation")
    @JavascriptFunction
    public void notify(@NonNull String message, @NonNull NotificationType type) {
        try {
            this.nativeSystem.notify(message, type);
        } catch (IllegalStateException ignored) {
            this.UI.showToast(message, NotificationType.NONE);
        }
    }

    @JavascriptGetter("useBetaKoiPath")
    public boolean isUseBetaKoiPath() {
        return this.appPreferences.get().isUseBetaKoiPath();
    }

    @JavascriptSetter("useBetaKoiPath")
    public void setUseBetaKoiPath(boolean val) {
        this.appPreferences.get().setUseBetaKoiPath(val);
        this.appPreferences.save();
    }

    @JavascriptGetter("enableStupidlyUnsafeSettings")
    public boolean enableStupidlyUnsafeSettings() {
        return this.UI.getPreferences().isEnableStupidlyUnsafeSettings();
    }

    @JavascriptGetter("clientId")
    private String caffeinatedClientId() {
        return caffeinatedClientId;
    }

    @Override
    public @Nullable CasterlabsAccount getCasterlabsAccount() {
        return this.auth.getCasterlabsAccount();
    }

    @JavascriptGetter("hasCasterlabsPlus")
    public boolean hasCasterlabsPlus() {
        CasterlabsAccount account = this.auth.getCasterlabsAccount();

        if (account == null) {
            return false;
        }

        return account.hasCasterlabsPlus();
    }

    @JavascriptFunction
    public boolean hasUpdate() {
        if (isDev) return false;

        try {
            String commit = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url(
                        String.format(
                            "https://cdn.casterlabs.co/dist/%s/commit",
                            this.buildInfo.getBuildChannel()
                        )
                    )
            );

            return !this.buildInfo.getCommit().equals(commit);
        } catch (Exception ignored) {
            return false;
        }
    }

}

package co.casterlabs.caffeinated.app;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.api.AppApi;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.auth.AuthPreferences;
import co.casterlabs.caffeinated.app.chatbot.AppChatbot;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences;
import co.casterlabs.caffeinated.app.chatbot.ChatbotScriptEngine;
import co.casterlabs.caffeinated.app.koi.GlobalKoi;
import co.casterlabs.caffeinated.app.locale._LocaleLoader;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.plugins.PluginIntegration;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.app.ui.ThemeManager;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugins;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.pluginsdk.Locale;
import co.casterlabs.caffeinated.pluginsdk.koi.TestEvents;
import co.casterlabs.caffeinated.util.ClipboardUtil;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.rakurai.io.http.MimeTypes;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.swetrix.Swetrix;
import dev.webview.webview_java.Webview;
import dev.webview.webview_java.bridge.JavascriptFunction;
import dev.webview.webview_java.bridge.JavascriptGetter;
import dev.webview.webview_java.bridge.JavascriptObject;
import dev.webview.webview_java.bridge.JavascriptSetter;
import dev.webview.webview_java.bridge.JavascriptValue;
import dev.webview.webview_java.bridge.WebviewBridge;
import lombok.AccessLevel;
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

    public static final String KOI_ID = "LmHG2ux992BxqQ7w9RJrfhkW";
    public static final String APP_DATA_DIR;

    public static final String AUTH_URL = "https://auth.casterlabs.co/auth/redirect";
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

    private @Getter(AccessLevel.NONE) LocaleProvider appLocale;

    private @JavascriptValue(allowSet = false) boolean isTraySupported;

    private @Getter(AccessLevel.NONE) Swetrix analytics;

    private Connection preferencesConnection;

    // @formatter:off

    // Integrations
    public final PluginIntegration  pluginIntegration  = new PluginIntegration();
//    public final Multistreaming     multistreaming     = Multistreaming.INSTANCE;
    public final MusicIntegration   music              = new MusicIntegration();
//    public final AppControlDeck     controlDeck        = new AppControlDeck();
    public final ThemeManager       themeManager       = new ThemeManager();
    public final AppChatbot         chatbot            = new AppChatbot();
    public final GlobalKoi          koi                = new GlobalKoi();
    public final AppAuth            auth               = new AppAuth();
    public final EmojisObj          emojis             = new EmojisObj();
    public final AppApi             api                = new AppApi();
    public final AppUI              UI                 = new AppUI();

    // Preferences
    public final PreferenceFile<ChatbotPreferences>           chatbotPreferences = new PreferenceFile<>("chatbot", ChatbotPreferences.class);
//    public final PreferenceFile<CaffeinatedWindowState>       windowPreferences = new PreferenceFile<>("window", CaffeinatedWindowState.class);
    public final PreferenceFile<AuthPreferences>              authPreferences = new PreferenceFile<>("auth", AuthPreferences.class);

    // @formatter:on

    @JavascriptValue(allowSet = false)
    public final PreferenceFile<AppPreferences> appPreferences = new PreferenceFile<>("app", AppPreferences.class);

    // Event stuff
    private Map<String, List<Consumer<JsonObject>>> appEventListeners = new HashMap<>();

    private final NativeSystem nativeSystem;

    private @Getter List<RealtimeApiListener> apiListeners = new LinkedList<>();

    static {
        AppDirs appDirs = AppDirsFactory.getInstance();
        APP_DATA_DIR = appDirs.getUserDataDir("casterlabs-caffeinated", null, null, true);

        new File(APP_DATA_DIR, "preferences").mkdirs();
        new File(APP_DATA_DIR, "preferences/old").mkdir();

        final File logsDir = new File(APP_DATA_DIR, "logs");
        final File logFile = new File(logsDir, "app.log");

        try {
            logsDir.mkdirs();
            logFile.delete();
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

        this.reloadLanguage();
    }

    @SneakyThrows
    public void init(boolean traySupported) {
        this.isTraySupported = traySupported;

        this.preferencesConnection = DriverManager.getConnection("jdbc:sqlite:" + new File(APP_DATA_DIR, "preferences/database.sqlite").getCanonicalPath());

        this.chatbot.init();
//        this.multistreaming.init();
        this.UI.init();
        this.themeManager.init();
        this.auth.init();
//        this.controlDeck.init();
        this.api.init();
        this.pluginIntegration.init();
//            this.koi.init();
        this.music.init();

        this.appPreferences.save();

        try {
            this.analytics.trackPageView("/", this.UI.getPreferences().getLanguage());
            this.analytics.startHeartbeat();
        } catch (Exception e) {
            FastLogger.logStatic(e);
        }

        ChatbotScriptEngine.class.toString(); // Load.

        Calendar calendar = Calendar.getInstance();
        int calendarMonth = calendar.get(Calendar.MONTH);
        int calendarDate = calendar.get(Calendar.DATE);

        if (calendarMonth == Calendar.OCTOBER && calendarDate == 31) {
            this.notify("Boo!", Collections.emptyMap(), NotificationType.WARNING);
        }

//        System.gc();
    }

    @Override
    public String getLocale() {
        return this.UI.getPreferences().getLanguage().toUpperCase();
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

    public boolean canDoOneTimeEvent(@NonNull String id) {
        return this.appPreferences.get().getOneTimeEvents().add(id);
    }

    public void reloadLanguage() {
        String locale = this.UI.getPreferences().getLanguage();

        this.appLocale = _LocaleLoader.load(locale);
    }

    @Override
    public void openLink(String url) {
        this.UI.openLink(url);
    }

    @Override
    public String getMimeForPath(String path) {
        return MimeTypes.getMimeForFile(new File(path));
    }

    @Override
    public CaffeinatedPlugins getPlugins() {
        return this.pluginIntegration.getPlugins();
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
    public void notify(@NonNull String message, Map<String, String> placeholders, @NonNull NotificationType type) {
        String localized = this.localize(message, placeholders, Collections.emptyList());

        try {
            this.nativeSystem.notify(localized, type);
        } catch (IllegalStateException ignored) {
            this.UI.showToast(localized, NotificationType.NONE);
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
        return KOI_ID;
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

    @Override
    public @NonNull String localize(String key, @Nullable Map<String, String> knownPlaceholders, @Nullable List<String> knownComponents) {
        if (key == null) return "";

        if (knownPlaceholders == null) knownPlaceholders = Collections.emptyMap();
        if (knownComponents == null) knownComponents = Collections.emptyList();

        String value = this.appLocale.process(key, null, knownPlaceholders, knownComponents);

        if (value == null) {
            // See if any of the plugins can localize the string.
            for (CaffeinatedPlugin plugin : this.pluginIntegration.getLoadedPlugins()) {
                @Nullable
                Map<String, LocaleProvider> fullLang = plugin.getLang();
                if (fullLang == null) continue;

                LocaleProvider lang = fullLang.get(this.getLocale().replace('-', '_').toUpperCase());
                if (lang == null) continue;

                value = lang.process(key, null, knownPlaceholders, knownComponents);
                if (value != null) break; // We found one!
            }
        }

        if (value == null) {
//            FastLogger.logStatic(LogLevel.WARNING, "Could not find locale key: %s", key);
            return key;
        }

        // Go over any supposed UI placeholders and see if regular ones will fit...
        for (Map.Entry<String, String> placeholder : knownPlaceholders.entrySet()) {
            if (knownComponents.contains(placeholder.getKey())) continue; // This UI will handle this...

            value = value.replace('%' + placeholder.getKey() + '%', placeholder.getValue());
        }

        return value;
    }

    @SneakyThrows
    @JavascriptFunction
    public @NonNull String localize(String key, @Nullable JsonObject knownPlaceholders, @Nullable JsonArray knownComponents) {
        if (key == null) return "";

        Map<String, String> knownPlaceholders_map = new HashMap<>();
        if (knownPlaceholders != null) {
            for (Entry<String, JsonElement> entry : knownPlaceholders.entrySet()) {
                if (entry.getValue().isJsonString()) {
                    knownPlaceholders_map.put(entry.getKey(), entry.getValue().getAsString());
                } else {
                    knownPlaceholders_map.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return this.localize(
            key,
            knownPlaceholders_map,
            Rson.DEFAULT.fromJson(knownComponents, new TypeToken<List<String>>() {
            })
        );
    }

    @JavascriptGetter("LOCALES")
    public Map<String, JsonObject> getLocalesMap() {
        Map<String, JsonObject> locales = new HashMap<>();
        for (Locale l : Locale.values()) {
            locales.put(l.name(), l.toJson());
        }
        return locales;
    }

    @SneakyThrows
    @JavascriptFunction
    public void globalTest(KoiEventType type) {
        // Pick a random account that we're signed-in to.
        UserUpdateEvent[] userStates = CaffeinatedApp.getInstance().getKoi().getUserStates().values().toArray(new UserUpdateEvent[0]);
        UserUpdateEvent randomAccount = userStates[ThreadLocalRandom.current().nextInt(userStates.length)];

        KoiEvent e = TestEvents.createTestEvent(type, randomAccount.getStreamer().getPlatform());
        if (e == null) return;
        this.koi.broadcastEvent(e);
    }

    public void track(String event, boolean unique) {
        try {
            this.analytics.track(event, unique);
        } catch (Exception e) {
            FastLogger.logStatic(e);
        }
    }

}

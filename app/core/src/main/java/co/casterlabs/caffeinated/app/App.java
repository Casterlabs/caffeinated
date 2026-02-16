package co.casterlabs.caffeinated.app;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import app.saucer.SaucerApp;
import app.saucer.SaucerFilePicker;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.api.AppApi;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.chatbot.AppChatbot;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.koi.KoiImpl;
import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.app.plugins.AppPlugins;
import co.casterlabs.caffeinated.app.ui.AppThemeManager;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.bootstrap.BuildInfo;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.pluginsdk.Locale;
import co.casterlabs.caffeinated.pluginsdk.koi.TestEvents;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.PlatformMessageEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.rich.fragments.TextFragment;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
@JavascriptObject
public class App {

    @JavascriptValue(value = "clientId", allowSet = false)
    public static final String KOI_ID = "LmHG2ux992BxqQ7w9RJrfhkW";

    public static final String AUTH_URL = "https://auth.casterlabs.co/auth/redirect";
    public static final Map<String, String> OVERRIDE_AUTH_URLS = Map.of(
        "spotify", "https://api.casterlabs.co/v2/caffeinated/spotify/auth/redirect",
        "streamlabs", "https://api.casterlabs.co/v2/caffeinated/streamlabs/auth/redirect"
    );

    public static @JavascriptValue(allowSet = false) BuildInfo buildInfo;
    public static @JavascriptValue(allowSet = false) boolean isDev;
    public static @JavascriptValue(allowSet = false) boolean isTraySupported;

    private static NativeSystem nativeSystem;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static JsonArray statusStates = JsonArray.EMPTY_ARRAY;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static boolean hasUpdate = false;

    @SneakyThrows
    public static void init(@NonNull BuildInfo buildInfo, boolean isDev, NativeSystem nativeSystem, boolean traySupported) {
        App.buildInfo = buildInfo;
        App.isDev = isDev;
        App.nativeSystem = nativeSystem;

        Currencies.getCurrencies(); // Load the class.

        AsyncTask.create(() -> {
            while (true) {
                try {
                    JsonObject response = Rson.DEFAULT.fromJson(
                        WebUtil.sendHttpRequest(
                            new Request.Builder()
                                .url("https://api.status.casterlabs.co")
                                .header("X-Installation-ID", AppConfig.appPreferences.get().installationId)
                        ), JsonObject.class
                    );

                    JsonArray statusStates = new JsonArray();
                    for (String only : Arrays.asList("Caffeinated")) {
                        if (!response.getObject("data").getObject("stateData").containsKey(only)) continue;
                        JsonObject state = response.getObject("data").getObject("stateData").getObject(only);
                        if (state.getString("status").equals("OPERATIONAL")) continue;
                        statusStates.add(state);
                    }
                    App.statusStates = statusStates;
                } catch (Throwable t) {
                    FastLogger.logStatic(LogLevel.WARNING, "Error whilst polling status API. Retrying later.\n%s", t);
                }
                try {
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException ignored) {}
            }
        });

        AsyncTask.create(() -> {
            while (true) {
                try {
                    String commit = WebUtil.sendHttpRequest(
                        new Request.Builder()
                            .url(
                                String.format(
                                    "https://cdn.casterlabs.co/caffeinated/dist/%s/commit",
                                    buildInfo.getBuildChannel()
                                )
                            )
                    );

                    hasUpdate = !buildInfo.getCommit().equals(commit);
                } catch (Throwable t) {
                    FastLogger.logStatic(LogLevel.WARNING, "Error whilst polling status API. Retrying later.\n%s", t);
                }
                try {
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException ignored) {}
            }
        });

        isTraySupported = traySupported;

        AppUI.updateIcon();

        try {
            AppChatbot.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }

        try {
            AppUI.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
        try {
            AppThemeManager.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
        try {
            AppAuth.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
        try {
            AppApi.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
        try {
            AppPlugins.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
        try {
            MusicImpl.INSTANCE.init();
        } catch (Throwable t) {
            FastLogger.logException(t);
        }

        AppConfig.appPreferences.save();

        Calendar calendar = Calendar.getInstance();
        int calendarMonth = calendar.get(Calendar.MONTH);
        int calendarDate = calendar.get(Calendar.DATE);

        if (calendarMonth == Calendar.OCTOBER && calendarDate == 31) {
            notify("Boo! 👻", Collections.emptyMap(), NotificationType.WARNING);
        }

        System.gc();
        System.gc();
        System.gc();
    }

    public static String getLocale() {
        return AppConfig.uiPreferences.get().getLanguage().toUpperCase();
    }

    public static boolean canCloseUI() {
        // We can prevent ui closure if needed.
        // Maybe during plugin installs?
        // TODO
        return true;
    }

    public static void shutdown() {
        AppAuth.shutdown();
    }

    /**
     * Sends a system notification, if that fails then it'll fallback on a UI-based
     * notification instead.
     */
    @JavascriptFunction
    public static void notify(@NonNull String message, Map<String, String> placeholders, @NonNull NotificationType type) {
        String localized = Caffeinated.getInstance().localize(message, placeholders, Collections.emptyList());

        try {
            nativeSystem.notify(localized, type);
        } catch (IllegalStateException ignored) {
            AppUI.showToast(localized, NotificationType.NONE);
        }

        switch (type) {
            case ERROR:
                KoiImpl.INSTANCE.broadcastEvent(
                    PlatformMessageEvent.of(
                        UserPlatform.CASTERLABS_SYSTEM,
                        UserPlatform.CASTERLABS_SYSTEM.systemProfile,
                        Instant.now(),
                        Arrays.asList(TextFragment.of("🚨 " + localized)),
                        Collections.emptyList(),
                        null
                    )
                );
                break;

            case WARNING:
                KoiImpl.INSTANCE.broadcastEvent(
                    PlatformMessageEvent.of(
                        UserPlatform.CASTERLABS_SYSTEM,
                        UserPlatform.CASTERLABS_SYSTEM.systemProfile,
                        Instant.now(),
                        Arrays.asList(TextFragment.of("⚠️ " + localized)),
                        Collections.emptyList(),
                        null
                    )
                );
                break;

            case INFO:
            case NONE:
                KoiImpl.INSTANCE.broadcastEvent(
                    PlatformMessageEvent.of(
                        UserPlatform.CASTERLABS_SYSTEM,
                        UserPlatform.CASTERLABS_SYSTEM.systemProfile,
                        Instant.now(),
                        Arrays.asList(TextFragment.of("ℹ️ " + localized)),
                        Collections.emptyList(),
                        null
                    )
                );
                break;
        }
    }

    @JavascriptGetter("LOCALES")
    public static Map<String, JsonObject> getLocalesMap() {
        Map<String, JsonObject> locales = new HashMap<>();
        for (Locale l : Locale.values()) {
            locales.put(l.name(), l.toJson());
        }
        return locales;
    }

    @SneakyThrows
    @JavascriptFunction
    public static void globalTest(KoiEventType type) {
        // Pick a random account that we're signed-in to.
        UserUpdateEvent[] userStates = KoiImpl.INSTANCE.getUserStates().values().toArray(new UserUpdateEvent[0]);
        UserUpdateEvent randomAccount = userStates[ThreadLocalRandom.current().nextInt(userStates.length)];

        KoiEvent e = TestEvents.createTestEvent(type, randomAccount.streamer.platform);
        if (e == null) return;
        KoiImpl.INSTANCE.broadcastEvent(e);
    }

    @JavascriptFunction
    public static String pickFile(String... filter) {
        return SaucerApp.dispatch(() -> {
            return pickFile_unsafe(filter);
        });
    }

    @SneakyThrows
    private static String pickFile_unsafe(String... filter) {
        SaucerFilePicker picker = SaucerFilePicker.create();

        picker.initial(new File(System.getProperty("user.home")));

        if (filter != null && filter.length > 0) {
            picker.filter(filter);
        }

        File result = picker.pickFile();
        if (result == null) {
            return null;
        }

        byte[] fileBytes = Files.readAllBytes(result.toPath());
        String mime = MimeTypes.getMimeForFile(result);
        String filename = result.getName();
        String base64 = Base64.getEncoder().encodeToString(fileBytes);

        return "data:" + mime + ";filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20") + ";base64," + base64;
    }

}

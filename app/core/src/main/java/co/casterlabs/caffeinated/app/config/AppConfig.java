package co.casterlabs.caffeinated.app.config;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.AppPreferences;
import co.casterlabs.caffeinated.app.auth.AuthPreferences;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences;
import co.casterlabs.caffeinated.app.ui.AppThemeManager;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.app.ui.ThemePreferences;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import lombok.NonNull;
import net.harawata.appdirs.AppDirsFactory;
import xyz.e3ndr.fastloggingframework.loggerimpl.FileLogHandler;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@JavascriptObject
public class AppConfig {
    public static final String APP_DATA_DIR = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    public static final Connection preferencesConnection;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    public static final PreferenceFile<AppPreferences> appPreferences = new PreferenceFile<>("app", AppPreferences.class);

    @JavascriptValue(allowSet = false, watchForMutate = true)
    public static final PreferenceFile<UIPreferences> uiPreferences = new PreferenceFile<>("ui", UIPreferences.class);

//    public static final PreferenceFile<CaffeinatedWindowState> windowPreferences  = new PreferenceFile<>("window", CaffeinatedWindowState.class);
    @JavascriptValue(allowSet = false, watchForMutate = true)
    public static final PreferenceFile<ChatbotPreferences> chatbotPreferences = new PreferenceFile<>("chatbot", ChatbotPreferences.class);

    @JavascriptValue(allowSet = false, watchForMutate = true)
    public static final PreferenceFile<AuthPreferences> authPreferences = new PreferenceFile<>("auth", AuthPreferences.class);

    @JavascriptValue(allowSet = false, watchForMutate = true)
    public static final PreferenceFile<ThemePreferences> themePreferences = new PreferenceFile<>("theme", ThemePreferences.class);

    static {
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

        Connection conn;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + new File(APP_DATA_DIR, "preferences/database.sqlite").getCanonicalPath());
        } catch (SQLException | IOException e) {
            RuntimeException rethrow = new RuntimeException("Failed to initialize preferences database connection.", e);
            FastLogger.logException(rethrow);
            throw rethrow;
        }
        preferencesConnection = conn;
    }

    @JavascriptFunction
    public static boolean canDoOneTimeEvent(@NonNull String id) {
        boolean result = appPreferences.get().oneTimeEvents.add(id);
        if (result) {
            appPreferences.save();
        }
        return result;
    }

    /* ---------------- */
    /*  Bridge Helpers  */
    /* ---------------- */

    @JavascriptSetter("appPreferences")
    static void setAppPreferences(@NonNull AppPreferences prefs) {
        appPreferences.set(prefs);
    }

    @JavascriptSetter("uiPreferences")
    static void setUIPreferences(@NonNull UIPreferences prefs) {
        uiPreferences.set(prefs);
        AppUI.onUpdatePreferences();
    }

    @JavascriptSetter("chatbotPreferences")
    static void setChatbotPreferences(@NonNull ChatbotPreferences prefs) {
        chatbotPreferences.set(prefs);
    }

    @JavascriptSetter("authPreferences")
    static void setAuthPreferences(@NonNull AuthPreferences prefs) {
        authPreferences.set(prefs);
    }

    @JavascriptSetter("themePreferences")
    static void setThemePreferences(@NonNull ThemePreferences prefs) {
        themePreferences.set(prefs);
        AppThemeManager.onUpdatePreferences();
    }

}

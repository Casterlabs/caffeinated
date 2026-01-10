package co.casterlabs.caffeinated.app.locale;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import glocale.Glocale;
import glocale.parser.RsonParser;
import glocale.part.Part;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@JavascriptObject
public class AppLocale {
    @JavascriptValue(value = "fallback", allowSet = false, watchForMutate = true)
    public static final JsonObject fallbackJson = loadJson("en_US");
    private static final Map<String, Part[]> fallback = RsonParser.parse(fallbackJson);

    @JavascriptValue(value = "current", allowSet = false, watchForMutate = true)
    public static JsonObject currentJson = fallbackJson;

    public static final Glocale glocale = new Glocale().use(fallback);

    @JavascriptValue(value = "available", allowSet = false)
    private static final Map<String, String> AVAILABLE_LOCALES = Map.of(
        "en_US", "English"
    );

    public static void onUpdatePreferences() {
        // TODO
//        String locale = AppConfig.uiPreferences.get().getLanguage();
//        currentJson = loadJson(locale);
//        glocale.use(RsonParser.parse(currentJson));
    }

    private static JsonObject loadJson(String locale) {
        try (InputStream in = AppLocale.class.getClassLoader().getResourceAsStream("co/casterlabs/caffeinated/app/locale/" + locale + ".json")) {
            String raw = StreamUtil.toString(in, StandardCharsets.UTF_8);
            return Rson.DEFAULT.fromJson(raw, JsonObject.class);
        } catch (Throwable t) {
            if (fallbackJson == null) {
                FastLogger.logStatic(LogLevel.FATAL, "Could not load app locale. Crashing!\n%s", t);
                throw new RuntimeException("Could not load app locale.", t);
            }

            FastLogger.logStatic(LogLevel.WARNING, "Could not load locale '%s', falling back to en_US.", locale);
            return fallbackJson;
        }
    }

}

package co.casterlabs.caffeinated.builtin.locale;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.commons.localization.helpers.JsonLocaleProvider;
import co.casterlabs.rakurai.json.Rson;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class BuiltInLocale {
    public static final Map<String, LocaleProvider> providers = Map.of(
        "EN_US", loadJson("en_US")
    );

    private static JsonLocaleProvider loadJson(String locale) {
        try (InputStream in = BuiltInLocale.class.getClassLoader().getResourceAsStream("co/casterlabs/caffeinated/builtin/locale/" + locale + ".json")) {
            String json = StreamUtil.toString(in, StandardCharsets.UTF_8);

            return Rson.DEFAULT.fromJson(json, JsonLocaleProvider.class);
        } catch (Throwable t) {
            new FastLogger("BUILTIN LOCALE UTIL").fatal("Could not load builtin locale. Ignoring!\n%s", t);
            return new JsonLocaleProvider();
        }
    }

}

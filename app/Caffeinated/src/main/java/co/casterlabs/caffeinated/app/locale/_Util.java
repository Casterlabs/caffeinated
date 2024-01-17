package co.casterlabs.caffeinated.app.locale;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.localization.helpers.JsonLocaleProvider;
import co.casterlabs.rakurai.json.Rson;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

class _Util {

    @SneakyThrows
    static JsonLocaleProvider loadJson(String locale) {
        try (InputStream in = _Util.class.getClassLoader().getResourceAsStream("co/casterlabs/caffeinated/app/locale/" + locale + ".json")) {
            String json = StreamUtil.toString(in, StandardCharsets.UTF_8);

            return Rson.DEFAULT.fromJson(json, JsonLocaleProvider.class);
        } catch (Throwable t) {
            new FastLogger("LOCALE UTIL").fatal("Could not load app locale. Crashing!\n%s", t);
            throw t;
        }
    }

}

package co.casterlabs.caffeinated.app.locale;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.localization.LocaleProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class _LocaleLoader {
    private static final LocaleProvider FALLBACK = new en_US().get();

    @SuppressWarnings("deprecation")
    @SneakyThrows
    public static LocaleProvider load(String locale) {
        LocaleProvider provider;

        switch (locale.toUpperCase()) {
            case "FR_FR":
                provider = fr_FR.class.newInstance().get();
                break;

            case "EN_US":
                return FALLBACK;

            default:
                FastLogger.logStatic(LogLevel.WARNING, "Unable to find locale provider for: %s", locale);
                return FALLBACK;
        }

        return new LocaleProvider() {
            @Override
            protected @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
                String result = provider.process(key, externalLookup, knownPlaceholders, knownComponents);

                if (result == null) {
                    FastLogger.logStatic(LogLevel.WARNING, "Unable to find locale key: %s", key);
                    return FALLBACK.process(key, externalLookup, knownPlaceholders, knownComponents);
                } else {
                    return result;
                }
            }
        };
    }

}

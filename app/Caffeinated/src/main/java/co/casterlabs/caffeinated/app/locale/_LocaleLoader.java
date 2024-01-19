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

        switch (locale.toUpperCase().replace('-', '_')) {
            case "DA_DK":
                provider = da_DK.class.newInstance().get();
                break;

            case "EN_US":
                return FALLBACK;

            case "ES_419":
                provider = es_419.class.newInstance().get();
                break;

            case "ES_ES":
                provider = es_ES.class.newInstance().get();
                break;

            case "FR_FR":
                provider = fr_FR.class.newInstance().get();
                break;

            case "ID_ID":
                provider = id_ID.class.newInstance().get();
                break;

            case "RU_RU":
                provider = ru_RU.class.newInstance().get();
                break;

            case "TR_TR":
                provider = tr_TR.class.newInstance().get();
                break;

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

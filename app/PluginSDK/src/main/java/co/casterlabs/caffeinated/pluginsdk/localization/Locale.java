package co.casterlabs.caffeinated.pluginsdk.localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class Locale implements LocaleProcessorFunction {
    private static final Pattern EXTERNAL_KEY_PATTERN = Pattern.compile("\\[[\\w\\.]+\\]");

    private Map<Language, List<LocaleProvider>> providers = new HashMap<>();
    {
        for (Language language : Language.values()) {
            this.providers.put(language, new ArrayList<>());
        }
    }

    @Getter
    @Setter
    @NonNull
    private Language currentLanguage = Language.EN_US;

    public Locale registerProvider(@NonNull LocaleProvider provider) {
        this.providers.get(provider.language()).add(provider);
        return this;
    }

    @Override
    public @NonNull String process(@NonNull String key, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        // Modify the placeholders.
        knownPlaceholders = new HashMap<>(knownPlaceholders);

        knownPlaceholders.put("meta.language.name", this.currentLanguage.name);
        knownPlaceholders.put("meta.language.code", this.currentLanguage.code);
        knownPlaceholders.put("meta.language.emoji", this.currentLanguage.emoji);
        knownPlaceholders.put("meta.language.direction", this.currentLanguage.direction.name());

        knownPlaceholders = Collections.unmodifiableMap(knownPlaceholders);
        knownComponents = Collections.unmodifiableList(new ArrayList<>(knownComponents)); // We want to speed this up as well.

        String value = key;

        for (LocaleProvider provider : this.providers.get(this.currentLanguage)) {
            if (!key.startsWith(provider.prefix())) continue; // Not applicable.

            String trueKey = key.substring(provider.prefix().length()); // Chop off the prefix.
            String providerResult = provider.process(trueKey, knownPlaceholders, knownComponents);

            if (providerResult != null) {
                value = providerResult;
                break; // We found a valid value!
            }
        }

        // Avoid mangling keys that do not have placeholders.
        if (key.endsWith(".raw") || key.endsWith(".code")) return value;

        for (Map.Entry<String, String> placeholder : knownPlaceholders.entrySet()) {
            value.replace(
                '{' + placeholder.getKey() + '}',
                placeholder.getValue()
            );
        }

        {
            Matcher m = EXTERNAL_KEY_PATTERN.matcher(value);
            while (m.find()) {
                String match = m.group();
                String matchKey = match.substring(1, match.length() - 1);
                String matchValue = this.process(matchKey, knownPlaceholders, knownComponents);

                value.replace(
                    match,
                    matchValue
                );
            }
        }

        return value;
    }

}

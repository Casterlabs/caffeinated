package co.casterlabs.caffeinated.pluginsdk.localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.Either;
import co.casterlabs.commons.functional.tuples.Pair;
import lombok.NonNull;

/**
 * Use {@link Builder} to build your own instance.
 */
public class FlexibleLocaleProvider implements LocaleProvider {
    private Builder config;

    @Override
    public String prefix() {
        return this.config.prefix;
    }

    @Override
    public Language language() {
        return this.config.language;
    }

    @Override
    public final @Nullable String process(@NonNull String key, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
        for (Pair<String, LocaleProcessorFunction> entry : this.config.regexes) {
            String regex = entry.a();
            LocaleProcessorFunction function = entry.b();

            if (key.matches(regex)) {
                return function.process(key, knownPlaceholders, knownComponents);
            }
        }

        Either<String, LocaleProcessorFunction> either = this.config.languageMap.get(key);
        if (either == null) return null;

        if (either.isA()) {
            return either.a();
        } else {
            return either.b().process(key, knownPlaceholders, knownComponents);
        }
    }

    public static class Builder {
        private Map<String, Either<String, LocaleProcessorFunction>> languageMap = new HashMap<>();
        private List<Pair<String, LocaleProcessorFunction>> regexes = new LinkedList<>();

        private String prefix;
        private Language language;

        public Builder(@NonNull String prefix, @NonNull Language language) {
            this.prefix = prefix;
            this.language = language;
        }

        public Builder(@NonNull FlexibleLocaleProvider copyFrom) {
            this.prefix = copyFrom.config.prefix;
            this.language = copyFrom.config.language;
            this.languageMap.putAll(copyFrom.config.languageMap);
        }

        public final Builder string(@NonNull String key, @NonNull String value) {
            this.languageMap.put(key, Either.newA(value));
            return this;
        }

        public final Builder function(@NonNull String key, @NonNull LocaleProcessorFunction function) {
            this.languageMap.put(key, Either.newB(function));
            return this;
        }

        public final Builder Rfunction(@NonNull String regex, @NonNull LocaleProcessorFunction function) {
            this.regexes.add(new Pair<>(regex, function));
            return this;
        }

        public FlexibleLocaleProvider build() {
            FlexibleLocaleProvider instance = new FlexibleLocaleProvider();
            this.languageMap = Collections.unmodifiableMap(this.languageMap);
            this.regexes = Collections.unmodifiableList(new ArrayList<>(this.regexes));
            instance.config = this;
            return instance;
        }

    }

}
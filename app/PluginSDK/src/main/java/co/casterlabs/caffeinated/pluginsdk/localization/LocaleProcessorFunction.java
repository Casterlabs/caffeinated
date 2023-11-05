package co.casterlabs.caffeinated.pluginsdk.localization;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public interface LocaleProcessorFunction {

    /**
     * The following placeholders apply: <br />
     * - <b>{placeholder}</b>: Plain text placeholder. <br />
     * - <b>%component%</b>: UI components. <br />
     * - <b>[key]</b>: Pulls another translation key into the string. <br />
     * <br />
     * 
     * Placeholders are replaced for you automatically EXCEPT for keys that end in
     * {@code .raw} or {@code .code}.
     * 
     * @implSpec Return null if you do not understand or have a key.
     */
    public @Nullable String process(@NonNull String key, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents);

}

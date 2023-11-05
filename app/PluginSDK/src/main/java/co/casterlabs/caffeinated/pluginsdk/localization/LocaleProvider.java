package co.casterlabs.caffeinated.pluginsdk.localization;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public interface LocaleProvider {

    /**
     * @implSpec This is a lookup prefix. All {@code key}s will have this prefix
     *           removed from them when {@link #process(Language, String)} is
     *           called.
     * 
     * @implNote For Caffeinated widgets, your namespace will ALWAYS be a part of
     *           your prefix. So if you have a key, {@code my.example.key}, you will
     *           need to reference it as {@code com.example.my.example.key}
     *           otherwise the language system will not know where to lookup your
     *           key. Your prefix will still need to be {@code my.example.key}.
     */
    public String prefix();

    public Language language();

    /**
     * The following placeholders apply: <br />
     * - <b>{placeholder}</b>: Plain text placeholder. <br />
     * - <b>[placeholder]</b>: Pulls another translation key into the string. <br />
     * - <b>%placeholder%</b>: UI components. <br />
     * 
     * @implSpec Return null if you do not understand or have a key.
     */
    public @Nullable String process(@NonNull String key);

}

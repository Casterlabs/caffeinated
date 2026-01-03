package co.casterlabs.caffeinated.pluginsdk;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngines;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public interface Caffeinated {

    @SneakyThrows
    public static Caffeinated getInstance() {
        return ReflectionLib.getStaticValue(Class.forName("co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl"), "INSTANCE");
    }

    public Koi getKoi();

    public Music getMusic();

    public Emojis getEmojis();

    public CaffeinatedPlugins getPlugins();

    public void copyText(@NonNull String text, @Nullable String toastText);

    public void openLink(String url);

    public String getMimeForPath(String path);

    public String getLocale();

    /**
     * @deprecated Use {@link #localize(String, Map, Map)} instead.
     */
    @Deprecated
    default @NonNull String localize(@NonNull String key, @Nullable Map<String, String> knownPlaceholders, @Nullable List<String> knownComponents) {
        return this.localize(key, knownPlaceholders, Collections.emptyMap());
    }

    /**
     * The following placeholders apply: <br />
     * - <b>{placeholder}</b>: Plain text placeholder. <br />
     * - <b>&lt;component&gt;</b>: UI components. <br />
     * - <b>[external_key]</b>: Pulls another translation key into the string.
     * <br />
     * <br />
     * 
     * @return a string, even if the specified key could not be found.
     */
    public @NonNull String localize(@NonNull String key, @Nullable Map<String, String> knownPlaceholders, @Nullable Map<String, String> knownComponents);

    public ScriptingEngines getScriptingEngines();

}

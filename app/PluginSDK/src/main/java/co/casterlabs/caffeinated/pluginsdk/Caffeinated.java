package co.casterlabs.caffeinated.pluginsdk;

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
        return ReflectionLib.invokeStaticMethod(Class.forName("co.casterlabs.caffeinated.app.CaffeinatedApp"), "getInstance");
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
     * The following placeholders apply: <br />
     * - <b>{placeholder}</b>: Plain text placeholder. <br />
     * - <b>%component%</b>: UI components. <br />
     * - <b>[external_key]</b>: Pulls another translation key into the string.
     * <br />
     * <br />
     * 
     * Placeholders are replaced for you automatically EXCEPT for keys that end in
     * {@code .raw} or {@code .code}.
     * 
     * @return a string, even if the specified key could not be found.
     */
    public @NonNull String localize(@NonNull String key, @Nullable Map<String, String> knownPlaceholders, @Nullable List<String> knownComponents);

    public ScriptingEngines getScriptingEngines();

}

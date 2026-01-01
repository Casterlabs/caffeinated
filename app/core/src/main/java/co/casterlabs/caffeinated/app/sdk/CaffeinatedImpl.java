package co.casterlabs.caffeinated.app.sdk;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import app.saucer.SaucerDesktop;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import co.casterlabs.caffeinated.app.App;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.app.plugins.AppPlugins;
import co.casterlabs.caffeinated.app.plugins.CaffeinatedPluginsImpl;
import co.casterlabs.caffeinated.app.scripting.ScriptingEnginesImpl;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugins;
import co.casterlabs.caffeinated.pluginsdk.Emojis;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngines;
import co.casterlabs.caffeinated.util.ClipboardUtil;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

@JavascriptObject
public class CaffeinatedImpl implements Caffeinated {
    public static final CaffeinatedImpl INSTANCE = new CaffeinatedImpl();

    @Override
    public Koi getKoi() {
        return KoiImpl.INSTANCE;
    }

    @Override
    public Music getMusic() {
        return MusicImpl.INSTANCE;
    }

    @Override
    public Emojis getEmojis() {
        return EmojisImpl.INSTANCE;
    }

    @Override
    public CaffeinatedPlugins getPlugins() {
        return CaffeinatedPluginsImpl.INSTANCE;
    }

    @Override
    public ScriptingEngines getScriptingEngines() {
        return ScriptingEnginesImpl.INSTANCE;
    }

    @SuppressWarnings("deprecation")
    @Override
    @JavascriptFunction
    public void copyText(@NonNull String text, @Nullable String toastText) {
        ClipboardUtil.copy(text);

        if (toastText != null) {
            AppUI.showToast(toastText, NotificationType.NONE);
        }
    }

    @Override
    @JavascriptFunction
    public void openLink(String url) {
        if (url.startsWith("#")) return; // Not a real link.
        SaucerDesktop.open(url);
    }

    @Override
    @JavascriptFunction
    public String getMimeForPath(String path) {
        return MimeTypes.getMimeForFile(new File(path));
    }

    @Override
    @JavascriptFunction
    public String getLocale() {
        return AppConfig.uiPreferences.get().getLanguage().toUpperCase();
    }

    @Override
    public @NonNull String localize(String key, @Nullable Map<String, String> knownPlaceholders, @Nullable List<String> knownComponents) {
        if (key == null) return "";

        if (knownPlaceholders == null) knownPlaceholders = Collections.emptyMap();
        if (knownComponents == null) knownComponents = Collections.emptyList();

        String value = App.appLocale.process(key, null, knownPlaceholders, knownComponents);

        if (value == null) {
            // See if any of the plugins can localize the string.
            for (CaffeinatedPlugin plugin : AppPlugins.getLoadedPlugins()) {
                @Nullable
                Map<String, LocaleProvider> fullLang = plugin.getLang();
                if (fullLang == null) continue;

                LocaleProvider lang = fullLang.get(this.getLocale().replace('-', '_').toUpperCase());
                if (lang == null) continue;

                value = lang.process(key, null, knownPlaceholders, knownComponents);
                if (value != null) break; // We found one!
            }
        }

        if (value == null) {
//            FastLogger.logStatic(LogLevel.WARNING, "Could not find locale key: %s", key);
            return key;
        }

        // Go over any supposed UI placeholders and see if regular ones will fit...
        for (Map.Entry<String, String> placeholder : knownPlaceholders.entrySet()) {
            if (knownComponents.contains(placeholder.getKey())) continue; // This UI will handle this...

            value = value.replace('%' + placeholder.getKey() + '%', placeholder.getValue());
        }

        return value;
    }

    @SneakyThrows
    @JavascriptFunction
    public @NonNull String localize(String key, @Nullable JsonObject knownPlaceholders, @Nullable JsonArray knownComponents) { // for the JS Bridge
        if (key == null) return "";

        Map<String, String> knownPlaceholders_map = new HashMap<>();
        if (knownPlaceholders != null) {
            for (Entry<String, JsonElement> entry : knownPlaceholders.entrySet()) {
                if (entry.getValue().isJsonString()) {
                    knownPlaceholders_map.put(entry.getKey(), entry.getValue().getAsString());
                } else {
                    knownPlaceholders_map.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return Caffeinated.getInstance().localize(
            key,
            knownPlaceholders_map,
            Rson.DEFAULT.fromJson(knownComponents, new TypeToken<List<String>>() {
            })
        );
    }

}

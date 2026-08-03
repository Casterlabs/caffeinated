package co.casterlabs.caffeinated.app.sdk;

import java.io.File;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.koi.KoiImpl;
import co.casterlabs.caffeinated.app.locale.AppLocale;
import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.app.plugins.CaffeinatedPluginsImpl;
import co.casterlabs.caffeinated.app.scripting.ScriptingEnginesImpl;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugins;
import co.casterlabs.caffeinated.pluginsdk.Emojis;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngines;
import co.casterlabs.caffeinated.util.ClipboardUtil;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.caffeinated.window.AppWindow;
import glocale.part.ComponentPart;
import glocale.part.Part;
import lombok.NonNull;

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
        AppWindow.INSTANCE.open(url);
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
    public @NonNull String localize(String key, @Nullable Map<String, String> knownPlaceholders, @Nullable Map<String, String> knownComponents) {
        if (key == null) return "";

        Part[] values;
        try {
            values = AppLocale.glocale.lookup(key);
        } catch (IllegalArgumentException e) {
            // Key not found, return the key itself.
            return key;
        }

        StringBuilder buf = new StringBuilder();
        for (Part value : values) {
            if (value.type() == Part.Type.COMPONENT) {
                ComponentPart componentPart = (ComponentPart) value;
                String replacement = knownComponents.get(componentPart.name);
                if (replacement != null) {
                    buf.append(replacement);
                }
                continue; // Skip unknown components.
            }
            String rendered = value.render(AppLocale.glocale, knownPlaceholders);
            buf.append(rendered);
        }

        return buf.toString();
    }

}

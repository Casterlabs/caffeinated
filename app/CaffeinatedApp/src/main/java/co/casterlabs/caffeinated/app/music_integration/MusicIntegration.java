package co.casterlabs.caffeinated.app.music_integration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider;
import co.casterlabs.caffeinated.app.music_integration.impl.SpotifyMusicProvider;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import co.casterlabs.caffeinated.pluginsdk.music.MusicPlaybackState;
import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class MusicIntegration extends JavascriptObject implements Music {
    private static InternalMusicProvider<?> systemPlaybackMusicProvider = null;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<String, InternalMusicProvider<?>> providers = new HashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private InternalMusicProvider<?> activePlayback;

    private boolean loaded = false;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, MusicProvider> getProviders() {
        return (Map<String, MusicProvider>) ((Object) this.providers); // Yucky cast
    }

    public void init() {
        this.updateBridgeData(); // Populate

        // Register the providers (in order of preference)
        new SpotifyMusicProvider(this);
        new PretzelMusicProvider(this);

        // Try to register the system playback music provider.
        if (systemPlaybackMusicProvider != null) {
            this.providers.put(systemPlaybackMusicProvider.getServiceId(), systemPlaybackMusicProvider);
            try {
                ReflectionLib.invokeMethod(systemPlaybackMusicProvider, "init");
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                FastLogger.logException(e);
            }
        }

        // Load their settings
        PreferenceFile<MusicIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getMusicIntegrationPreferences();
        JsonObject prefsSettings = prefs.get().getSettings();
        for (Map.Entry<String, InternalMusicProvider<?>> entry : this.providers.entrySet()) {
            InternalMusicProvider<?> provider = entry.getValue();
            String providerId = entry.getKey();

            // Doesn't matter if it's null, we check for that inside of
            // MusicProvider#updateSettingsFromJson
            provider.updateSettingsFromJson(prefsSettings.get(providerId));
        }

        this.loaded = true;
        this.save();
    }

    public void save() {
        if (this.loaded) {
            PreferenceFile<MusicIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getMusicIntegrationPreferences();
            JsonObject prefsSettings = prefs.get().getSettings();

            for (InternalMusicProvider<?> provider : this.providers.values()) {
                prefsSettings.put(provider.getServiceId(), Rson.DEFAULT.toJson(provider.getSettings()));
            }

            prefs.save();

            this.updateBridgeData();
        }
    }

    @JavascriptFunction
    public void signoutMusicProvider(@NonNull String platform) {
        this.providers.get(platform).signout();
    }

    @JavascriptFunction
    public void updateMusicProviderSettings(@NonNull String platform, @NonNull JsonObject settings) {
        this.providers.get(platform).updateSettingsFromJson(settings);
    }

    public void updateBridgeData() {
        InternalMusicProvider<?> pausedTrack = null;
        InternalMusicProvider<?> playingTrack = null;

        for (InternalMusicProvider<?> provider : this.providers.values()) {
            if ((pausedTrack == null) && (provider.getPlaybackState() == MusicPlaybackState.PAUSED)) {
                pausedTrack = provider;
            } else if ((playingTrack == null) && (provider.getPlaybackState() == MusicPlaybackState.PLAYING)) {
                playingTrack = provider;
            }
        }

        if (playingTrack != null) {
            this.activePlayback = playingTrack;
        } else if (pausedTrack != null) {
            this.activePlayback = pausedTrack;
        } else {
            this.activePlayback = null;
        }

        final File musicApiDir = new File(CaffeinatedApp.appDataDir, "api/music");
        musicApiDir.mkdirs();

        // Write some cute files for the end user to mess with :^)
        new AsyncTask(() -> {
            try {
                Files.writeString(
                    new File(musicApiDir, "current_playback.json").toPath(),
                    Rson.DEFAULT.toJsonString(this.activePlayback)
                );

                Files.writeString(
                    new File(musicApiDir, "current_playback.txt").toPath(),
                    this.activePlayback == null ? ""
                        : String.format(
                            "%s • %s",
                            this.activePlayback.getCurrentTrack().getTitle(),
                            String.join(", ", this.activePlayback.getCurrentTrack().getArtists())
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Broadcast to the plugins & the music api.
        new AsyncTask(() -> {
            try {
                @SuppressWarnings("deprecation")
                JsonObject music = this.toJson();

                // Send the events to the widget instances.
                for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPlugins().getLoadedPlugins()) {
                    for (Widget widget : plugin.getWidgets()) {
                        for (WidgetInstance instance : widget.getWidgetInstances()) {
                            try {
                                instance.onMusicUpdate(music);
                            } catch (IOException ignored) {}
                        }
                    }
                }

                CaffeinatedApp.getInstance().getApi().musicApi.sendSong();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}

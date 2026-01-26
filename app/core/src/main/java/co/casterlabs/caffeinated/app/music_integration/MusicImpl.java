package co.casterlabs.caffeinated.app.music_integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.AppEventBus;
import co.casterlabs.caffeinated.app.api.AppApi;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.plugins.AppPlugins;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import co.casterlabs.caffeinated.pluginsdk.music.MusicPlaybackState;
import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.yen.Cache;
import co.casterlabs.yen.impl.SQLBackedCache;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@JavascriptObject
public class MusicImpl implements Music {
    public static final MusicImpl INSTANCE = new MusicImpl();

    private static AbstractMusicProvider<?> systemPlaybackMusicProvider = null;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<String, AbstractMusicProvider<?>> providers = new HashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private AbstractMusicProvider<?> activePlayback;

    private Cache<MusicProviderSettings> preferenceData;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, MusicProvider> getProviders() {
        return (Map<String, MusicProvider>) ((Object) this.providers); // Yucky cast
    }

    @Override
    public MusicProvider getActivePlayback() {
        return this.activePlayback;
    }

    @SneakyThrows
    public void init() {
        this.preferenceData = new SQLBackedCache<>(-1, AppConfig.preferencesConnection, "kv_music");

        // Migrate.
        this.importOldJson();

        // Register the providers (in order of preference)
        new SpotifyMusicProvider(this);
        new PretzelMusicProvider(this);
        if (systemPlaybackMusicProvider != null) this.providers.put(systemPlaybackMusicProvider.getServiceId(), systemPlaybackMusicProvider);

        // Load their settings and init.
        for (Map.Entry<String, AbstractMusicProvider<?>> entry : this.providers.entrySet()) {
            AbstractMusicProvider<?> provider = entry.getValue();
            String providerId = entry.getKey();

            MusicProviderSettings settings = this.preferenceData.get(providerId);
            if (settings == null) {
                provider.updateSettingsFromJson(null);
            } else {
                provider.updateSettingsFromJson(settings.getJson());
            }

            provider.init();
        }

        this.updateBridgeData(); // Populate
    }

    public void save(AbstractMusicProvider<?> provider) {
        this.preferenceData.submit(MusicProviderSettings.from(provider));
        this.updateBridgeData();
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
        AbstractMusicProvider<?> pausedTrack = null;
        AbstractMusicProvider<?> playingTrack = null;

        for (AbstractMusicProvider<?> provider : this.providers.values()) {
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

        final File musicApiDir = new File(AppConfig.APP_DATA_DIR, "api/music");
        musicApiDir.mkdirs();

        // Write some cute files for the end user to mess with :^)
        AsyncTask.create(() -> {
            try {
                Files.writeString(
                    new File(musicApiDir, "current_playback.json").toPath(),
                    Rson.DEFAULT.toJson(this.activePlayback).toString()
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

        @SuppressWarnings("deprecation")
        JsonObject music = this.toJson();

        // Broadcast to the plugins & the music api.
        AsyncTask.create(() -> {
            try {
                // Send the events to the widget instances.
                for (CaffeinatedPlugin plugin : AppPlugins.getLoadedPlugins()) {
                    for (Widget widget : plugin.getWidgets()) {
                        for (WidgetInstance instance : widget.getWidgetInstances()) {
                            try {
                                instance.onMusicUpdate(music);
                            } catch (IOException ignored) {}
                        }
                    }
                }

                AppApi.musicApi.sendSong();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Broadcast to the local api.
        AsyncTask.create(() -> {
            AppEventBus.bus.post(AppEventBus.MUSIC_UPDATE, music);
        });
    }

    private void importOldJson() {
        File oldJson = new File(AppConfig.APP_DATA_DIR, "preferences/music.json");
        if (!oldJson.exists()) return;

        try {
            FastLogger.logStatic(LogLevel.INFO, "Found old music.json, importing...");

            JsonObject json = Rson.DEFAULT.fromJson(new String(Files.readAllBytes(oldJson.toPath()), StandardCharsets.UTF_8), JsonObject.class);
            JsonObject settings = json.getObject("settings");

            for (Entry<String, JsonElement> entry : settings.entrySet()) {
                this.preferenceData.submit(MusicProviderSettings.from(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.WARNING, "Could not import old plugins.json:\n%s", e);
        } finally {
            // Keep a backup of the file.
            FastLogger.logStatic(LogLevel.INFO, "Done!");
            oldJson.renameTo(new File(AppConfig.APP_DATA_DIR, "preferences/old/music.json"));
            oldJson.delete();
        }
    }

}

package co.casterlabs.caffeinated.app.music_integration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.events.AppMusicIntegrationEventType;
import co.casterlabs.caffeinated.app.music_integration.events.AppMusicIntegrationSettingsUpdateEvent;
import co.casterlabs.caffeinated.app.music_integration.events.AppMusicIntegrationSignoutEvent;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider;
import co.casterlabs.caffeinated.app.music_integration.impl.SpotifyMusicProvider;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import co.casterlabs.caffeinated.pluginsdk.music.MusicPlaybackState;
import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.BridgeValue;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class MusicIntegration extends Music {
    private static EventHandler<AppMusicIntegrationEventType> handler = new EventHandler<>();

    private static InternalMusicProvider<?> systemPlaybackMusicProvider = null;

    private Map<String, InternalMusicProvider<?>> providers = new HashMap<>();
    private InternalMusicProvider<?> activePlayback;

    private boolean loaded = false;

    private BridgeValue<JsonObject> bridge = new BridgeValue<>("music");

    @SneakyThrows
    public MusicIntegration() {
        handler.register(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, MusicProvider> getProviders() {
        return (Map<String, MusicProvider>) ((Object) this.providers);
    }

    public void init() {
        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.bridge);
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

    @EventListener
    public void onMusicIntegrationSignoutEvent(AppMusicIntegrationSignoutEvent event) {
        this.providers.get(event.getPlatform()).signout();
    }

    @EventListener
    public void onMusicIntegrationSettingsUpdateEvent(AppMusicIntegrationSettingsUpdateEvent event) {
        this.providers.get(event.getPlatform()).updateSettingsFromJson(event.getSettings());
    }

    public void updateBridgeData() {
        JsonObject musicServices = new JsonObject();

        InternalMusicProvider<?> pausedTrack = null;
        InternalMusicProvider<?> playingTrack = null;

        for (InternalMusicProvider<?> provider : this.providers.values()) {
            musicServices.put(provider.getServiceId(), Rson.DEFAULT.toJson(provider));

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

        JsonObject bridgeData = new JsonObject()
            .put("activePlayback", Rson.DEFAULT.toJson(this.activePlayback))
            .put("musicServices", musicServices);

        this.bridge.set(bridgeData);

        new AsyncTask(() -> {
            try {
                @SuppressWarnings("deprecation")
                JsonObject music = this.toJson();

                // Send the events to the widget instances.
                for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
                    for (Widget widget : plugin.getWidgets()) {
                        for (WidgetInstance instance : widget.getWidgetInstances()) {
                            try {
                                instance.onMusicUpdate(music);
                            } catch (IOException ignored) {}
                        }
                    }
                }

                CaffeinatedApp.getInstance().getApi().sendSong();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppMusicIntegrationEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}

package co.casterlabs.caffeinated.app.music_integration.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider.PretzelSettings;
import co.casterlabs.caffeinated.pluginsdk.music.MusicTrack;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class PretzelMusicProvider extends InternalMusicProvider<PretzelSettings> {
    private static final String PRETZEL_ENDPOINT = "https://api.pretzel.tv/playing/twitch/%s/json";
    private static final long POLL_RATE = TimeUnit.SECONDS.toMillis(20);

    private FastLogger logger = new FastLogger();

    private String channelId;
    private MusicTrack currentTrackCache;

    public PretzelMusicProvider(@NonNull MusicIntegration musicIntegration) {
        super("Pretzel", "pretzel", PretzelSettings.class);
        musicIntegration.getProviders().put(this.getServiceId(), this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void init() {
        CaffeinatedApp.getInstance().onAppEvent("auth:platforms", (JsonObject data) -> {
            try {
                if (data.containsKey("TWITCH")) {
                    JsonObject twitchUserData = data
                        .getObject("TWITCH")
                        .getObject("userData");

                    String id = twitchUserData.getString("channel_id");

                    if ((this.channelId == null) || !this.channelId.equals(id)) {
                        this.logger.info("Now signed in as: %s", twitchUserData.getString("displayname"));

                        this.channelId = id;

                        this.setAccountData(
                            true,
                            String.format("Twitch: %s", twitchUserData.getString("displayname")),
                            "https://play.pretzel.rocks"
                        );

                        AsyncTask.create(() -> {
                            this.pollPretzel();
                        });
                    }
                } else {
                    this.channelId = null;
                    this.setAccountData(false, null, null);
                }
            } catch (Exception e) {
                FastLogger.logException(e);
            }
        });

        AsyncTask.create(() -> {
            while (true) {
                this.pollPretzel();
                try {
                    Thread.sleep(POLL_RATE);
                } catch (InterruptedException ignored) {}
            }
        });
    }

    private void pollPretzel() {
        if (this.isSignedIn() && this.isEnabled()) {
            try {
                String response = WebUtil.sendHttpRequest(new Request.Builder().url(String.format(PRETZEL_ENDPOINT, this.channelId)));

                JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);
                // Janky check for ratelimit.
                if (json.containsKey("track")) {
                    String songLink = json.getString("url");
                    String title = json.getObject("track").getString("title");
                    String artworkUrl = json.getObject("track").getString("artworkUrl");
                    List<String> artists = Arrays.asList(json.getObject("track").getString("artist"));

                    // *thanos meme*
                    // All that, for a music track?
                    this.currentTrackCache = new MusicTrack(
                        title,
                        artists,
                        null,
                        artworkUrl,
                        songLink
                    );

                    // this.logger.info("Polled Pretzel successfully.");
                } /* else {
                     this.logger.debug("Error while polling Pretzel: %s", response);
                  }*/

                if (this.currentTrackCache != null) {
                    this.setPlaying(this.currentTrackCache);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.currentTrackCache = null;
        }
    }

    @Override
    protected void onSettingsUpdate() {
        if (this.isEnabled()) {
            this.pollPretzel();
        } else {
            this.setPlaybackStateInactive();
        }
    }

    @Override
    public void signout() {} // NO-OP

    public boolean isEnabled() {
        return this.getSettings().enabled;
    }

    @JsonClass(exposeAll = true)
    public static class PretzelSettings {
        private boolean enabled = false;

    }

}

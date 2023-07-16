package co.casterlabs.caffeinated.app.music_integration.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.impl.SpotifyMusicProvider.SpotifySettings;
import co.casterlabs.caffeinated.pluginsdk.music.MusicTrack;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class SpotifyMusicProvider extends InternalMusicProvider<SpotifySettings> {
    private static final long POLL_RATE = TimeUnit.SECONDS.toMillis(15);

    private FastLogger logger = new FastLogger();

    private String refreshToken;
    private String accessToken;

    public SpotifyMusicProvider(@NonNull MusicIntegration musicIntegration) {
        super("Spotify", "spotify", SpotifySettings.class);
        musicIntegration.getProviders().put(this.getServiceId(), this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void init() {
        CaffeinatedApp
            .getInstance()
            .onAppEvent("auth:completion", (JsonObject data) -> {
                if (data.getString("type").equals("music") &&
                    data.getString("platform").equals("spotify")) {
                    this.logger.info("Completing OAuth.");
                    this.completeOAuth();
                }
            });

        AsyncTask.create(() -> {
            while (true) {
                this.pollSpotify();
                try {
                    Thread.sleep(POLL_RATE);
                } catch (InterruptedException ignored) {}
            }
        });
    }

    private void pollSpotify() {
        if (!this.isSignedIn()) return;

        try {
            if (this.accessToken == null) {
                JsonObject response = Rson.DEFAULT.fromJson(
                    WebUtil.sendHttpRequest(
                        new Request.Builder()
                            .url("https://api.casterlabs.co/v2/caffeinated/spotify/auth/refresh?refresh_token=" + this.refreshToken)
                    ), JsonObject.class
                );

                if (response.containsKey("error")) {
                    this.signout();
                    return;
                }

                this.logger.debug("Refreshed token.");

                this.accessToken = "Bearer " + response.getString("access_token");

                // TODO if (response.containsKey("refresh_token")) {} update prefs

                JsonObject profile = Rson.DEFAULT.fromJson(
                    WebUtil.sendHttpRequest(
                        new Request.Builder()
                            .url("https://api.spotify.com/v1/me")
                            .header("content-type", "application/json")
                            .header("authorization", this.accessToken)
                    ), JsonObject.class
                );

                this.setAccountData(
                    true,
                    profile.getString("display_name"),
                    profile.getObject("external_urls").getString("spotify")
                );

                this.logger.debug("Refreshed profile.");
            }

            JsonObject response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(
                    new Request.Builder()
                        .url("https://api.spotify.com/v1/me/player")
                        .header("content-type", "application/json")
                        .header("authorization", this.accessToken)
                ), JsonObject.class
            );

            if (response.containsKey("error")) {
                int responseStatus = response.getObject("error").getNumber("status").intValue();

                if (responseStatus == 401) {
                    this.accessToken = null;
                    this.pollSpotify();
                }
                return;
            }

            if (response.containsKey("item") && response.get("item").isJsonObject()) {
                JsonObject item = response.getObject("item");

                boolean isPlaying = response.getBoolean("is_playing");
                String songLink = item.getObject("external_urls").getString("spotify");
                String albumArtUrl = item.getObject("album").getArray("images").getObject(0).getString("url");
                String album = item.getObject("album").getString("name");
                String title = item.getString("name");
                List<String> artists = new LinkedList<>();

                for (JsonElement artist : item.getArray("artists")) {
                    artists.add(artist.getAsObject().getString("name"));
                }

                // Use the better parsing for a more accurate result.
                Pair<String, List<String>> betterResult = InternalMusicProvider.parseTitleForArtists(title, artists);
                title = betterResult.a();
                artists = betterResult.b();

                MusicTrack track = new MusicTrack(title, artists, album, albumArtUrl, songLink);

                if (isPlaying) {
                    this.setPlaying(track);
                } else {
                    this.setPaused(track);
                }
            } else {
                this.makePaused();
            }
        } catch (StringIndexOutOfBoundsException | JsonParseException ignored) {
            // ignored.
        } catch (Throwable t) {
            FastLogger.logException(t);
        }
    }

    @Override
    protected void onSettingsUpdate() {
        this.refreshToken = CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .getToken("music", "spotify");

        if (this.refreshToken == null) {
            this.setAccountData(false, null, null);
        } else {
            this.setAccountData(true, "?", "https://example.com");
            this.pollSpotify();
        }
    }

    @Override
    public void signout() {
        this.refreshToken = null;
        this.accessToken = null;

        this.setAccountData(false, null, null);
        CaffeinatedApp.getInstance().getAuthPreferences().get().removeToken("music", "spotify");
        CaffeinatedApp.getInstance().getAuthPreferences().save();
    }

    @JsonClass(exposeAll = true)
    public static class SpotifySettings {

    }

    private void completeOAuth() {
        String code = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("music", "spotify");

        try {
            JsonObject response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(
                    new Request.Builder()
                        .url("https://api.casterlabs.co/v2/caffeinated/spotify/auth/code?code=" + code)
                ), JsonObject.class
            );

            this.refreshToken = response.getString("refresh_token");

            // Update the auth file.
            CaffeinatedApp.getInstance().getAuthPreferences().get().addToken("music", "spotify", this.refreshToken);
            CaffeinatedApp.getInstance().getAuthPreferences().save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.logger.info("OAuth Completed, enabling now.");

        CaffeinatedApp.getInstance().getAuthPreferences().save();
        this.setAccountData(true, "?", "#");
        this.pollSpotify();
    }

}

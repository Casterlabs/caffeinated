package co.casterlabs.caffeinated.app.music_integration.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.InternalMusicProvider;
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
    private static final String AUTH_PROXY_URL = "https://api.casterlabs.co/v2/natsukashii/spotify";
    private static final long POLL_RATE = TimeUnit.SECONDS.toMillis(15);

    private FastLogger logger = new FastLogger();

    private String refreshToken;
    private String accessToken;

    public SpotifyMusicProvider(@NonNull MusicIntegration musicIntegration) {
        super("Spotify", "spotify", SpotifySettings.class);
        musicIntegration.getProviders().put(this.getServiceId(), this);

        CaffeinatedApp
            .getInstance()
            .onAppEvent("auth:completion", (JsonObject data) -> {
                if (data.getString("platform").equals("caffeinated_spotify")) {
                    this.logger.info("Completing OAuth.");
                    this.completeOAuth(data.getString("tokenId"));
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
        if (this.isSignedIn()) {
            try {
                if (this.accessToken == null) {
                    JsonObject response = Rson.DEFAULT.fromJson(
                        WebUtil.sendHttpRequest(
                            new Request.Builder()
                                .url(String.format("%s?refresh_token=%s", AUTH_PROXY_URL, this.refreshToken))
                        ), JsonObject.class
                    );

                    if (response.containsKey("error")) {
                        this.signout();
                        return;
                    } else {
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
                        return;
                    }
                } else {
                    if (response.containsKey("item")) {
                        if (response.get("item").isJsonObject()) {
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
                    }
                }
            } catch (StringIndexOutOfBoundsException | JsonParseException ignored) {
                // ignored.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSettingsUpdate() {
        String tokenId = this.getSettings().tokenId;

        if (tokenId == null) {
            this.setAccountData(false, null, null);
        } else {
            this.refreshToken = CaffeinatedApp
                .getInstance()
                .getAuthPreferences()
                .get()
                .getTokens()
                .getString(tokenId);

            this.setAccountData(true, "?", "https://example.com");
            this.pollSpotify();
        }
    }

    @Override
    public void signout() {
        String tokenId = this.getSettings().tokenId;

        if (tokenId != null) {
            this.refreshToken = null;
            this.accessToken = null;

            // Update the tokenId.
            this.getSettings().tokenId = null;
            this.updateSettings(this.getSettings());

            CaffeinatedApp.getInstance().getAuthPreferences().get().getTokens().remove(tokenId);
            CaffeinatedApp.getInstance().getAuthPreferences().save();
        }
    }

    @JsonClass(exposeAll = true)
    public static class SpotifySettings {
        private String tokenId;

    }

    private void completeOAuth(String tokenId) {
        String code = CaffeinatedApp.getInstance().getAuthPreferences().get().getTokens().getString(tokenId);

        try {
            JsonObject response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(
                    new Request.Builder()
                        .url(String.format("%s?code=%s", AUTH_PROXY_URL, code))
                ), JsonObject.class
            );

            String refreshToken = response.getString("refresh_token");

            // Update the auth file.
            CaffeinatedApp.getInstance().getAuthPreferences().get().getTokens().put(tokenId, refreshToken);
            CaffeinatedApp.getInstance().getAuthPreferences().save();

            FastLogger.logStatic("RefreshToken: ", refreshToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.logger.info("OAuth Completed, enabling now.");

        // Update the tokenId.
        this.getSettings().tokenId = tokenId;
        this.updateSettings(this.getSettings());

        CaffeinatedApp.getInstance().getAuthPreferences().save();

        this.pollSpotify();
    }

}

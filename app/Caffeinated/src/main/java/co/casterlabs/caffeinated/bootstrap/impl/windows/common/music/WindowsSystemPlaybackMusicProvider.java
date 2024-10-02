package co.casterlabs.caffeinated.bootstrap.impl.windows.common.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.app.music_integration.impl.InternalMusicProvider;
import co.casterlabs.caffeinated.bootstrap.SystemPlaybackMusicProvider;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.events.PlaybackEvent;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.events.PlaybackEvent.PlaybackEventType;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.events.PlaybackStateEvent;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.MediaInfo;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.PlaybackStatus;
import co.casterlabs.caffeinated.pluginsdk.music.MusicTrack;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class WindowsSystemPlaybackMusicProvider extends SystemPlaybackMusicProvider {
    private static final File WMCJCW_LOCATION = new File("./WMC-JsonConsoleWrapper.exe");

    private Map<String, SessionState> sessions = new HashMap<>();

    @Override
    public void init() {
        if (!WMCJCW_LOCATION.exists()) {
//            JOptionPane.showMessageDialog(null, "Unable to find " + WMCJCW_LOCATION.getAbsolutePath() + ".\nThe system music service will not work.", "Casterlabs-Caffeinated", JOptionPane.ERROR_MESSAGE, null);
            return;
        }

        Thread th = new Thread(this::startListener);
        th.setDaemon(true);
        th.setName("WindowsSystemPlaybackMusicProvider");
        th.start();
    }

    @SneakyThrows
    private void startListener() {
        Process p = new ProcessBuilder()
            .command(WMCJCW_LOCATION.getAbsolutePath()/*, "no_thumbnail"*/)
            .redirectOutput(Redirect.PIPE)
            .redirectInput(Redirect.PIPE)
            .start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = null;
        while ((line = reader.readLine()) != null) {
            try {
                JsonObject json = Rson.DEFAULT.fromJson(line, JsonObject.class);
                PlaybackEventType type = PlaybackEventType.valueOf(json.getString("Type"));
                String sessionId = json.getString("SessionId");

                switch (type) {
                    case PLAYBACK_STATE_CHANGE:
                        this.onPlaybackStateEvent(
                            new PlaybackEvent<PlaybackStateEvent>(
                                type,
                                sessionId,
                                Rson.DEFAULT.fromJson(json.get("Data"), PlaybackStateEvent.class)
                            )
                        );
                        break;

                    case PROPERTY_CHANGE:
                        this.onPropertyChange(
                            new PlaybackEvent<MediaInfo>(
                                type,
                                sessionId,
                                Rson.DEFAULT.fromJson(json.get("Data"), MediaInfo.class)
                            )
                        );
                        break;

                    case SESSION_CLOSED:
                        this.onSessionState(
                            new PlaybackEvent<Void>(
                                type,
                                sessionId,
                                null
                            ),
                            false
                        );
                        break;

                    case SESSION_STARTED:
                        this.onSessionState(
                            new PlaybackEvent<Void>(
                                type,
                                sessionId,
                                null
                            ),
                            true
                        );
                        break;
                }
            } catch (Exception e) {
                FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst parsing system music data:");
                FastLogger.logException(e);
            }
        }
    }

    private void onSessionState(PlaybackEvent<Void> e, boolean isStarted) {
        if (isStarted) {
            this.sessions.put(e.getSessionId(), new SessionState(e.getSessionId()));
        } else {
            this.sessions.remove(e.getSessionId());
            this.update();
        }
    }

    private void onPlaybackStateEvent(PlaybackEvent<PlaybackStateEvent> e) {
        SessionState session = this.sessions.get(e.getSessionId());

        session
            .setStatus(e.getData().getStatus())
            .setType(e.getData().getType());

        this.update();
    }

    private void onPropertyChange(PlaybackEvent<MediaInfo> e) {
        SessionState session = this.sessions.get(e.getSessionId());

        if (session != null) {
            session
                .setMediaInfo(e.getData())
                .setType(e.getData().getPlaybackType());

            this.update();
        }
    }

    @Override
    protected synchronized void update() {
        if (this.isEnabled()) {
            SessionState currentSession = null;

            // Grab the current audio session.
            {
                for (SessionState state : this.sessions.values()) {
                    // If status == PLAYING
                    if ((state.getStatus() == PlaybackStatus.PLAYING) ||
                        ((state.getStatus() == null) && (state.getMediaInfo() != null))) {
                        currentSession = state;
                        break;
                    }
                }

                if (currentSession == null) {
                    for (SessionState state : this.sessions.values()) {
                        // If status == null and MediaInfo != null
                        if ((state.getStatus() == null) && (state.getMediaInfo() != null)) {
                            currentSession = state;
                            break;
                        }
                    }
                }
            }

            if (currentSession != null) {
                String artistsString;
                String title;

                if (currentSession.getMediaInfo().getArtist().isEmpty()) {
                    // Try to fallback on the album artist.
                    if (!currentSession.getMediaInfo().getAlbumArtist().isEmpty()) {
                        title = currentSession.getMediaInfo().getTitle();
                        artistsString = currentSession.getMediaInfo().getAlbumArtist();
                    } else {
                        // Fallback on parsing the title.
                        String[] splitTitle = currentSession.getMediaInfo().getTitle().split(" · ", 2);

                        title = splitTitle[0];

                        if (splitTitle.length > 1) {
                            artistsString = splitTitle[1];
                        } else {
                            artistsString = "Unknown Artist";
                        }
                    }
                } else {
                    title = currentSession.getMediaInfo().getTitle();
                    artistsString = currentSession.getMediaInfo().getArtist();
                }

                String album = currentSession.getMediaInfo().getAlbumTitle();
                String albumArtUrl = currentSession.getMediaInfo().getThumnailAsDataUri();
                List<String> artists = Arrays.asList(artistsString.split(", "));

                // Use the better parsing for a more accurate result.
                Pair<String, List<String>> betterResult = InternalMusicProvider.parseTitleForArtists(title, artists);

                title = betterResult.a();
                artists = betterResult.b();

                MusicTrack track = new MusicTrack(title, artists, album, albumArtUrl, "");

                this.setPlaying(track);
                return;
            }
        }

        this.setPlaybackStateInactive();
    }

}

package co.casterlabs.caffeinated.bootstrap.impl.linux.common;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.music_integration.impl.InternalMusicProvider;
import co.casterlabs.caffeinated.bootstrap.SystemPlaybackMusicProvider;
import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.caffeinated.pluginsdk.music.MusicTrack;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.io.streams.StreamUtil;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class LinuxSystemPlaybackMusicProvider extends SystemPlaybackMusicProvider {

    private Map<String, String> metadata;

    @Override
    public void init() {
        AsyncTask.create(() -> {
            while (true) {
                this.metadata = getPlayerMetaData();
                this.update();

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) {}
            }
        });
    }

    @Override
    protected void update() {
        if (this.isEnabled()) {
            if ((this.metadata != null) &&
                this.metadata.containsKey("xesam:title")) {
                // Get parsing!
                String album = this.metadata.getOrDefault("xesam:album", "");
                List<String> artists = Arrays.asList(this.metadata.getOrDefault("xesam:artist", "").split(", "));
                String title = this.metadata.getOrDefault("xesam:title", "");
                String albumArtUrl = MusicProvider.BLANK_ART;

                if (this.metadata.containsKey("mpris:artUrl")) {
                    String artUrl = this.metadata.get("mpris:artUrl");
                    String mimeType = MimeTypes.getMimeForType(artUrl.substring(artUrl.lastIndexOf('.')));

                    FastLogger.logStatic(artUrl);

                    byte[] bytes = urlToBytes(artUrl);
                    String b64 = Base64.getEncoder().encodeToString(bytes);

                    albumArtUrl = "data:" + mimeType + ";base64," + b64;
                }

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

    @SneakyThrows
    private static byte[] urlToBytes(String url) {
        InputStream in = new URL(url).openStream();
        return StreamUtil.toBytes(in);
    }

    private static @Nullable Map<String, String> getPlayerMetaData() {
        try {
            Map<String, String> metadata = new HashMap<>();

            String[] playerMetadata = execute("playerctl metadata").split("\n");

            for (String row : playerMetadata) {
                String[] columns = row.split(" ", 3);

//                String source = columns[0].trim();
                String entry = columns[1].trim();
                String value = columns[2].trim();

                metadata.put(entry, value);
            }

            return metadata;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @SneakyThrows
    private static String execute(String command) {
        Process p = new ProcessBuilder()
            .command("bash", "-c", command)
            .start();

        return StreamUtil.toString(p.getInputStream(), Charset.defaultCharset());
    }

    @SneakyThrows
    public static boolean isPlayerCtlInstalled() {
        Process p = new ProcessBuilder()
            .command("bash", "-c", "command -v playerctl")
            .start();

        int exitCode = p.waitFor();

        return exitCode == 0;
    }

}

package co.casterlabs.caffeinated.app.fun.multistreaming;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.caffeinated.util.archives.ArchiveExtractor;
import co.casterlabs.caffeinated.util.archives.ArchiveFormat;
import co.casterlabs.caffeinated.util.archives.ExtractionPlan;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.io.sink.SinkBuffer;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Multistreaming extends JavascriptObject {
    private static final String FFMPEG_INSTALL_URL = "https://github.com/GyanD/codexffmpeg/releases/download/6.0/ffmpeg-6.0-full_build-shared.7z";
    private static final String FFMPEG_VERSION = "6.0";

    static final File INSTALL_DIRECTORY = new File(CaffeinatedApp.APP_DATA_DIR, "lib/ffmpeg");

    public static final Multistreaming INSTANCE = new Multistreaming();
    static final FastLogger LOGGER = new FastLogger("Multistreaming");

    // Constants (Javascript)
    @JavascriptValue(allowSet = false)
    public final String RTMP_URL = "rtmp://127.0.0.1:1935/live";

    @JavascriptValue(allowSet = false)
    public final boolean IS_SUPPORTED = Platform.osDistribution == OSDistribution.WINDOWS_NT;

    // State.
    @JavascriptValue(watchForMutate = true, allowSet = false)
    private boolean installed = false;

    @JavascriptValue(watchForMutate = true, allowSet = false)
    boolean isReceivingVideo = false;

    @JavascriptValue(watchForMutate = true, allowSet = false)
    boolean isListening = false;

    // Actual meat and potatoes.
    private Set<Pair<Process, SinkBuffer>> targetProcesses = new HashSet<>();

    /* ---------------- */
    /* Video Loop       */
    /* ---------------- */

    void processPacket(byte[] packet) {
        // TODO
    }

    /* ---------------- */
    /* Installation     */
    /* ---------------- */

    public void init() {
        if (!this.IS_SUPPORTED) return;

        INSTALL_DIRECTORY.mkdirs();

        try {
            String installedVersion = Files.readString(new File(INSTALL_DIRECTORY, "version.txt").toPath());

            if (installedVersion.equals(FFMPEG_VERSION)) {
                // We're installed!.
                this.installed = true;
            }
        } catch (IOException e) {
            LOGGER.debug("Version check failed!\n%s", e);
        }
    }

    public void reinstall() throws IOException {
        if (!this.IS_SUPPORTED) return;

        Files
            .walk(INSTALL_DIRECTORY.toPath())
            .sorted(Comparator.reverseOrder()) // Delete the deepest first.
            .forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {}
            });

        INSTALL_DIRECTORY.mkdirs();

        File archiveFile = new File(INSTALL_DIRECTORY, "temp.7z");
        archiveFile.delete(); // Delete if it exists.

        try ( //
            Response response = WebUtil.client.newCall(
                new Request.Builder()
                    .url(FFMPEG_INSTALL_URL)
                    .build()
            ).execute();
            FileOutputStream targetStream = new FileOutputStream(archiveFile) //
        ) {
            archiveFile.createNewFile();
            StreamUtil.streamTransfer(response.body().byteStream(), targetStream, 2048);
        }

        ArchiveExtractor.extract(
            ArchiveFormat._7ZIP,
            archiveFile,
            INSTALL_DIRECTORY,
            ExtractionPlan.complex(
                "ffmpeg-6.0-full_build-shared",
                null,
                new String[] {
                        "/presets/.*",
                        "/lib/.*",
                        "/include/.*",
                        "doc/.*"
                }
            )
        );

        archiveFile.delete();
        this.installed = true;
    }

    /* ---------------- */
    /* Helpers          */
    /* ---------------- */

    static {
        // We need to make sure we terminate all of these during shutdown.
        Runtime
            .getRuntime()
            .addShutdownHook(new Thread(() -> {
                Listener.close();
                for (Pair<Process, SinkBuffer> p : INSTANCE.targetProcesses) {
                    p.a().destroyForcibly();
                }
            }));
    }

}

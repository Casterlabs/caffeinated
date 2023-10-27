package co.casterlabs.caffeinated.app.fun.multistreaming;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import co.casterlabs.commons.async.AsyncTask;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

class Listener {
    private static final FastLogger LOGGER = Multistreaming.LOGGER.createChild("Listener");

    private static boolean running = false;

    private static Process ffmpeg;

    private static void doLoop() {
        boolean isFirstListen = true;

        while (running) {
            LOGGER.debug("Proc starting.");

            try {
                ffmpeg = new ProcessBuilder()
                    .command(
                        new File(Multistreaming.INSTALL_DIRECTORY, "bin/ffmpeg").getCanonicalPath(),
                        "-hide_banner",
                        "-f", "flv",
                        "-listen", "1",
                        "-rw_timeout", "10",
                        "-timeout", "30",
                        "-i", "rtmp://0.0.0.0:1935/live",
                        "-c", "copy",
                        "-f", "nut", "-"
                    )
                    .redirectInput(Redirect.PIPE)
                    .redirectOutput(Redirect.PIPE)
                    .redirectError(Redirect.PIPE)
                    .start();

                // Read the FFMPEG log and write it to our console using FastLoggingFramework.
                AsyncTask.create(() -> {
                    FastLogger ffmpegLogger = LOGGER.createChild("FFMpeg");
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            ffmpegLogger.debug(line);
                            // TODO parse this for statistics.
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                LOGGER.debug("Proc started.");
                if (isFirstListen) {
                    isFirstListen = false;
                    Multistreaming.INSTANCE.isListening = true;
                    LOGGER.info("Listening on %s!\nWaiting for connection...", Multistreaming.INSTANCE.RTMP_URL);
                }

                boolean hasReceivedPacket = false;
                InputStream videoStream = ffmpeg.getInputStream();

                byte[] buf = new byte[2048];
                int read;
                while ((read = videoStream.read(buf)) != -1) {
                    if (hasReceivedPacket) {
                        hasReceivedPacket = true;
                        Multistreaming.INSTANCE.isReceivingVideo = true;
                        Multistreaming.LOGGER.info("Stream started.");
                    }

                    byte[] packet = new byte[read];
                    System.arraycopy(buf, 0, packet, 0, read);

                    Multistreaming.INSTANCE.processPacket(packet);
                }

                if (hasReceivedPacket) {
                    LOGGER.info("Stream ended.");
                    isFirstListen = true;
                    Multistreaming.INSTANCE.isReceivingVideo = false;
                    // TODO cleanup.
                }
                // Otherwise we have no need to clean up as we haven't started streaming yet.
            } catch (IOException e) {
                LOGGER.debug(e);
            } finally {
                LOGGER.debug("Proc closed.");
            }
        }

        Multistreaming.INSTANCE.isListening = false;
    }

    static void start() throws IOException {
        if (running) return;

        running = true;
        AsyncTask.create(Listener::doLoop);
    }

    static void close() {
        if (!running) return;

        running = false;
        ffmpeg.destroyForcibly();
    }

}

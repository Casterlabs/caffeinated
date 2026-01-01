package co.casterlabs.caffeinated.app;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.window.SaucerWindow;
import co.casterlabs.rakurai.json.element.JsonNumber;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import lombok.SneakyThrows;

@JavascriptObject
public class AppSounds {
    private static final SaucerWebview saucer;

    static {
        SaucerWindow window = SaucerWindow.create();
        saucer = window.createWebview((opts) -> {
            opts.hardwareAcceleration(true);

            switch (SaucerApp.backendType()) {
                case WEBVIEW2:
                    opts.appendBrowserFlag("-msWebView2SimulateMemoryPressureWhenInactive=true");
                    break;
                default:
                    break; // N/A
            }
        });

        saucer.window.title("Casterlabs-Caffeinated - Audio Player");
        saucer.contextMenuAllowed(false);

        saucer.url(SaucerUrl.parse("data:text/html;charset=utf-8,I'm an audio player!"));
    }

    @JavascriptFunction
    public static void playUrl(@NonNull String audioUrl, float volume) {
        if (audioUrl.startsWith("file://")) {
            File file = new File(audioUrl.substring("file://".length()));
            playFile(file, volume);
            return;
        }

        saucer.bridge.executeJavaScript(
            "(() => {"
                + "let previousAudioPromise = window.currentAudioPromise;"
                + "window.currentAudioPromise = new Promise(async (resolve) => {"
                + "  if (previousAudioPromise) await previousAudioPromise;"
                + "  const audio = new Audio(" + new JsonString(audioUrl) + ");"
                + "  audio.addEventListener('ended', resolve);"
                + "  audio.addEventListener('error', resolve);"
                + "  audio.volume = " + new JsonNumber(volume) + ";"
                + "  audio.play();"
                + "});"
                + "})();"
        );
    }

    public static void playBytes(@NonNull byte[] bytes, float volume) {
        String audioUrl = "data:audio/wav;base64," + Base64.getEncoder().encodeToString(bytes);
        playUrl(audioUrl, volume);
    }

    @SneakyThrows
    public static void playFile(@NonNull File file, float volume) {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        playBytes(fileBytes, volume);
    }

}

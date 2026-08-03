package co.casterlabs.caffeinated.window.saucer;

import java.io.File;

import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.window.SaucerWindow;
import co.casterlabs.caffeinated.window.AppSounds;
import co.casterlabs.rakurai.json.element.JsonNumber;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;

@JavascriptObject
public class _SaucerAppSounds extends AppSounds {
    private final SaucerWebview saucer;

    {
        SaucerWindow window = SaucerWindow.create();
        this.saucer = window.createWebview((opts) -> {
            opts.hardwareAcceleration(true);

            switch (SaucerApp.backendType()) {
                case WEBVIEW2:
                    opts.appendBrowserFlag("-msWebView2SimulateMemoryPressureWhenInactive=true");
                    break;
                default:
                    break; // N/A
            }
        });

        this.saucer.window.title("Casterlabs-Caffeinated - Audio Player");
        this.saucer.contextMenuAllowed(false);

        this.saucer.url(SaucerUrl.parse("data:text/html;charset=utf-8,I'm an audio player!"));
    }

    @Override
    @JavascriptFunction
    public void playUrl(@NonNull String audioUrl, float volume, float rate) {
        if (audioUrl.startsWith("file://")) {
            File file = new File(audioUrl.substring("file://".length()));
            playFile(file, volume, rate);
            return;
        }

        this.saucer.bridge.executeJavaScript(
            "(() => {"
                + "let previousAudioPromise = window.currentAudioPromise;"
                + "window.currentAudioPromise = new Promise(async (resolve) => {"
                + "  if (previousAudioPromise) await previousAudioPromise;"
                + "  const audio = new Audio(" + new JsonString(audioUrl) + ");"
                + "  audio.addEventListener('ended', resolve);"
                + "  audio.addEventListener('error', resolve);"
                + "  audio.volume = " + new JsonNumber(volume) + ";"
                + "  audio.playbackRate = " + new JsonNumber(rate) + ";"
                + "  audio.play();"
                + "});"
                + "})();"
        );
    }

}

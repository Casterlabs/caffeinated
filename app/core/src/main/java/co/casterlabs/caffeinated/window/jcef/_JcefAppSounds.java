package co.casterlabs.caffeinated.window.jcef;

import java.io.File;

import javax.swing.JFrame;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import app.saucer.bridge.JavascriptObject;
import co.casterlabs.caffeinated.window.AppSounds;
import co.casterlabs.rakurai.json.element.JsonNumber;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;

@JavascriptObject
public class _JcefAppSounds extends AppSounds {
    private final CefClient client;
    private final CefBrowser browser;

    {
        this.client = _CefUtil.createCefClient();
        this.browser = this.client.createBrowser("data:text/html;charset=utf-8,I'm an audio player!", _CefUtil.ENABLE_OSR, false);

        // Trick the browser into thinking it has been painted
        // so that it can play audio.
        JFrame testFrame = new JFrame("Casterlabs-Caffeinated - Audio Player");
        testFrame.add(this.browser.getUIComponent());
        testFrame.setSize(100, 100);
        testFrame.setUndecorated(true);
        testFrame.addNotify(); // Bad form, but it works.

        this.browser.getUIComponent().paint(testFrame.getGraphics());
    }

    @Override
    public void playUrl(@NonNull String audioUrl, float volume, float rate) {
        if (audioUrl.startsWith("file://")) {
            File file = new File(audioUrl.substring("file://".length()));
            playFile(file, volume, rate);
            return;
        }

        this.browser.executeJavaScript(
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
                + "})();",
            "inline",
            0
        );
    }

}

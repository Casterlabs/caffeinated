package co.casterlabs.caffeinated.app.window;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import app.saucer.bridge.JavascriptFunction;
import co.casterlabs.caffeinated.app.window.saucer._SaucerAppSounds;
import lombok.NonNull;
import lombok.SneakyThrows;

public abstract class AppSounds {
    public static final AppSounds INSTANCE = new _SaucerAppSounds();

    @JavascriptFunction
    public abstract void playUrl(@NonNull String audioUrl, float volume, float rate);

    public void playBytes(@NonNull byte[] bytes, float volume, float rate) {
        String audioUrl = "data:audio/wav;base64," + Base64.getEncoder().encodeToString(bytes);
        playUrl(audioUrl, volume, rate);
    }

    @SneakyThrows
    public void playFile(@NonNull File file, float volume, float rate) {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        playBytes(fileBytes, volume, rate);
    }

}

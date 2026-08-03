package co.casterlabs.caffeinated.window;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@SuppressWarnings("deprecation")
@JavascriptObject
public abstract class AppSounds {
    public static final AppSounds INSTANCE;

    static {
        List<Throwable> errors = new ArrayList<>();

        AppSounds instance = null;

        try {
            Class<?> clazz = Class.forName("co.casterlabs.caffeinated.window.jcef._JcefAppSounds");
            instance = (AppSounds) clazz.newInstance();
            FastLogger.logStatic("Using JCEF for the AppSounds.");
        } catch (Throwable t) {
            errors.add(t);
        }

        if (instance == null) {
            try {
                Class<?> clazz = Class.forName("co.casterlabs.caffeinated.window.saucer._SaucerAppSounds");
                instance = (AppSounds) clazz.newInstance();
                FastLogger.logStatic("Using Saucer for the AppSounds.");
            } catch (Throwable t) {
                errors.add(t);
            }
        }

        if (instance == null) {
            RuntimeException e = new RuntimeException("Failed to initialize AppSounds.");
            errors.forEach(e::addSuppressed);
            throw e;
        }

        INSTANCE = instance;
    }

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

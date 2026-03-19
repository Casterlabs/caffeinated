package co.casterlabs.caffeinated.pluginsdk;

import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginResource {
    public final byte[] data;
    public final String mimeType;

    public static PluginResource of(@NonNull byte[] data, @Nullable String mimeType) {
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return new PluginResource(data, mimeType);
    }

    public static PluginResource html(@NonNull String html) {
        return new PluginResource(html.getBytes(StandardCharsets.UTF_8), "text/html; charset=utf-8");
    }

}

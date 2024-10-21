package co.casterlabs.caffeinated.pluginsdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.Request;

public class TTS {
    private static @Getter List<String> voices = Collections.emptyList();

    // TODO create the v3 speech api and use that instead.
    static {
        try {
            JsonArray response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(new Request.Builder().url("https://api.casterlabs.co/v1/polly?request=voices")),
                JsonArray.class
            );

            List<String> voicesList = new ArrayList<>(response.size());

            for (JsonElement e : response) {
                voicesList.add(e.getAsString());
            }

            voices = Collections.unmodifiableList(voicesList);
        } catch (IOException uh_oh) {}
    }

    public static String[] getVoicesAsArray() {
        return voices.toArray(new String[0]);
    }

    public static byte[] getSpeech(@NonNull String defaultVoice, @NonNull String text) throws IOException {
        return WebUtil.sendHttpRequestBytes(
            new Request.Builder()
                .url(
                    getSpeechAsUrl(defaultVoice, text)
                )
        );
    }

    public static String getSpeechAsUrl(@NonNull String defaultVoice, @NonNull String text) throws IOException {
        assert doesVoiceExist(defaultVoice) : "Invalid voice";

        return String.format(
            "https://api.casterlabs.co/v1/polly?request=speech&voice=%s&text=%s",
            defaultVoice,
            WebUtil.encodeURIComponent(text)
        );
    }

    public static boolean doesVoiceExist(@Nullable String voice) {
        if (voice == null) {
            return false;
        } else {
            for (String v : voices) {
                if (v.toLowerCase().contains(voice.toLowerCase())) {
                    return true;
                }
            }

            return false;
        }
    }

}

package co.casterlabs.caffeinated.pluginsdk.koi;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.emoji.generator.WebUtil;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;

public class TestEvents {

    @SneakyThrows
    public static KoiEvent createTestEvent(@NonNull KoiEventType type, @Nullable UserPlatform platform) {
        String response;

        if (platform == null) {
            response = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url(String.format("https://api.casterlabs.co/v2/koi/test-event/%s", type.name()))
            );
        } else {
            response = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url(String.format("https://api.casterlabs.co/v2/koi/test-event/%s?platform=%s", type.name(), platform.name()))
            );
        }

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        return KoiEventType.get(json.getObject("data"));
    }

}

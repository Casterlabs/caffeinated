package co.casterlabs.koi.api.stream;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonClass(exposeAll = true, serializer = KoiStreamConfigurationSerializer.class)
public class KoiStreamConfiguration {
    private String title;
    private String category;
    private KoiStreamLanguage language;
    private List<String> tags;
    private @Nullable String thumbnail;
    private @JsonField("content_rating") KoiStreamContentRating contentRating;

}

class KoiStreamConfigurationSerializer implements JsonSerializer<KoiStreamConfiguration> {

    public KoiStreamConfigurationSerializer() {} // For Rson.

    @Override
    public JsonElement serialize(@NonNull Object value, @NonNull Rson rson) {
        JsonObject result = (JsonObject) DEFAULT.serialize(value, rson);

        if (result.get("thumbnail").isJsonNull()) {
            result.remove("thumbnail");
        }

        return result;
    }

}

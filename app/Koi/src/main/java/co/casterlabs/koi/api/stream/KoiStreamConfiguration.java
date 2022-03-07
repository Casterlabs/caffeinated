package co.casterlabs.koi.api.stream;

import java.util.Base64;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonClass(exposeAll = true, serializer = KoiStreamConfigurationSerializer.class)
public class KoiStreamConfiguration {
    private String title;
    private String category;
    private KoiStreamLanguage language;
    private List<String> tags;
    private @Nullable byte[] thumbnail;
    private @JsonField("content_rating") KoiStreamContentRating contentRating;

    @JsonSerializationMethod("thumbnail")
    private JsonElement $serialize_thumbnail() {
        if (this.thumbnail == null) {
            return null;
        } else {
            return new JsonString(
                Base64
                    .getMimeEncoder()
                    .encodeToString(this.thumbnail)
            );
        }
    }

}

class KoiStreamConfigurationSerializer implements JsonSerializer<KoiStreamConfiguration> {

    @Override
    public JsonElement serialize(@NonNull Object value, @NonNull Rson rson) {
        JsonObject result = (JsonObject) DEFAULT.serialize(value, rson);

        if (result.get("thumbnail").isJsonNull()) {
            result.remove("thumbnail");
        }

        return result;
    }

}

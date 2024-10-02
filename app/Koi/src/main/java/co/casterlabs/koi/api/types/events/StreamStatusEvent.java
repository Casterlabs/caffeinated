package co.casterlabs.koi.api.types.events;

import java.time.Instant;
import java.util.List;

import co.casterlabs.koi.api.stream.KoiStreamContentRating;
import co.casterlabs.koi.api.stream.KoiStreamLanguage;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class StreamStatusEvent extends KoiEvent {
    @JsonField("is_live")
    private boolean live;

    private String title;

    private Instant startTime;

    private List<String> tags;

    private String category;

    @JsonField("content_rating")
    private KoiStreamContentRating contentRating;

    @JsonField("thumbnail_url")
    private String thumbnailUrl;

    private KoiStreamLanguage language;

    @JsonDeserializationMethod("start_time")
    private void $deserialize_start_time(JsonElement e) {
        if (e.isJsonString()) {
            this.startTime = Instant.parse(e.getAsString());
        }
    }

    @JsonSerializationMethod("start_time")
    private JsonElement $serialize_start_time() {
        if (this.startTime == null) {
            return JsonNull.INSTANCE;
        } else {
            return new JsonString(this.startTime.toString());
        }
    }

    @Override
    public KoiEventType getType() {
        return KoiEventType.STREAM_STATUS;
    }

}

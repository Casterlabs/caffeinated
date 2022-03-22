package co.casterlabs.koi.api.types.events;

import java.time.Instant;
import java.util.List;

import co.casterlabs.koi.api.stream.KoiStreamContentRating;
import co.casterlabs.koi.api.stream.KoiStreamLanguage;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
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

    @JsonField("start_time")
    private Instant startTime;

    private List<String> tags;

    private String category;

    @JsonField("content_rating")
    private KoiStreamContentRating contentRating;

    @JsonField("thumbnail_url")
    private String thumbnailUrl;

    private KoiStreamLanguage language;

    @Override
    public KoiEventType getType() {
        return KoiEventType.STREAM_STATUS;
    }

}

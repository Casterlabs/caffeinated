package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class StreamStatusEvent extends KoiEvent {
    @JsonField("is_live")
    private boolean live;
    private String title;
    @JsonField("start_time")
    private String startTime;

    @Override
    public KoiEventType getType() {
        return KoiEventType.STREAM_STATUS;
    }

}

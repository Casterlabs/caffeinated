package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class PlatformMessageEvent extends ChatEvent {
    @JsonField("is_error")
    private boolean isError;

    @Override
    public KoiEventType getType() {
        return KoiEventType.PLATFORM_MESSAGE;
    }

}

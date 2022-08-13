package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class PlatformMessageEvent extends RichMessageEvent {

    @Override
    public KoiEventType getType() {
        return KoiEventType.PLATFORM_MESSAGE;
    }

}

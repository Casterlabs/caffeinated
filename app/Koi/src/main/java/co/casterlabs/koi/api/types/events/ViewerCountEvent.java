package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class ViewerCountEvent extends KoiEvent {
    private int count;

    @Override
    public KoiEventType getType() {
        return KoiEventType.VIEWER_COUNT;
    }

}

package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonArray;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class CatchupEvent extends KoiEvent {
    private JsonArray events;

    @Override
    public KoiEventType getType() {
        return KoiEventType.CATCHUP;
    }

}

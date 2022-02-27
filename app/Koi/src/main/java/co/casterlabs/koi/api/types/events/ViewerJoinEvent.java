package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class ViewerJoinEvent extends KoiEvent {
    private User viewer;

    @Override
    public KoiEventType getType() {
        return KoiEventType.VIEWER_JOIN;
    }

}

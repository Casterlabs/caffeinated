package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonClass(exposeAll = true)
public abstract class KoiEvent {
    protected User streamer;

    @JsonField("event_type")
    protected final KoiEventType type = this.getType(); // For RSON.

    @JsonField("event_abilities")
    protected EventAbilities abilities;

    public abstract KoiEventType getType();

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class EventAbilities {
        private boolean upvotable = false;
        private boolean deletable = false;

    }

}

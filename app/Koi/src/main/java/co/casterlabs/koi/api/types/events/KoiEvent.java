package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonClass(exposeAll = true)
public abstract class KoiEvent {
    protected SimpleProfile streamer;

    @Deprecated
    @JsonField("event_abilities")
    protected EventAbilities abilities;

    public abstract KoiEventType getType();

    @JsonSerializationMethod("event_type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType());
    }

    @Getter
    @ToString
    @Deprecated
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class EventAbilities {
        private boolean upvotable = false;
        private boolean deletable = false;

    }

}

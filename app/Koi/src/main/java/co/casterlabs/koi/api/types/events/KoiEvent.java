package co.casterlabs.koi.api.types.events;

import java.time.Instant;

import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
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
    private @JsonExclude Instant timestamp;

    @JsonField("event_abilities")
    protected EventAbilities abilities;

    public abstract KoiEventType getType();

    @JsonSerializationMethod("event_type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType());
    }

    @JsonSerializationMethod("timestamp")
    private JsonElement $serialize_timestamp() {
        return new JsonString(this.timestamp.toString());
    }

    @JsonDeserializationMethod("timestamp")
    private void $deserialize_timestamp(JsonElement e) {
        this.timestamp = Instant.parse(e.getAsString());
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

package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class UserUpdateEvent extends KoiEvent {
    private User streamer;

    // Intercept the deserialization.
    @JsonDeserializationMethod("streamer")
    private void $deserialize_streamer(JsonElement e) throws JsonValidationException, JsonParseException {
        this.streamer = Rson.DEFAULT.fromJson(e, User.class);
    }

    @JsonSerializationMethod("streamer")
    private JsonElement $serialize_streamer() {
        return Rson.DEFAULT.toJson(this.streamer);
    }

    @Override
    public KoiEventType getType() {
        return KoiEventType.USER_UPDATE;
    }

}

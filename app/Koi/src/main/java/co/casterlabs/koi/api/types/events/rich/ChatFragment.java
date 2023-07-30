package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public abstract class ChatFragment {
    private String raw;
    private String html;

    public abstract FragmentType getType();

    @JsonSerializationMethod("type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType());
    }

    public static enum FragmentType {
        TEXT,
        EMOTE,
        EMOJI,
        MENTION,
        LINK
    }

}

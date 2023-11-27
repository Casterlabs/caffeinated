package co.casterlabs.koi.api.types.events;

import java.util.HashSet;
import java.util.Set;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonBoolean;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonClass(exposeAll = true)
public abstract class MessageMeta extends KoiEvent {

    @JsonField("is_visible")
    private @Setter boolean visible = true;

    private @Setter int upvotes = 0;

    private @Getter Set<MessageAttribute> attributes = new HashSet<>();

    @JsonSerializationMethod("is_highlighted")
    private JsonBoolean $serialize_is_highlighted() {
        return new JsonBoolean(this.attributes.contains(MessageAttribute.HIGHLIGHTED));
    }

    /**
     * Use {@link #getAttributes()}.contains(MessageAttribute.HIGHLIGHTED).
     */
    @Deprecated
    public boolean isHighlighted() {
        return this.attributes.contains(MessageAttribute.HIGHLIGHTED);
    }

    public void apply(MessageMeta other) {
        other.visible = this.visible;
        other.attributes = new HashSet<>(this.attributes);

        // Only apply upvotes if we think this instance is current.
        // Sometimes it won't be, and we don't want a message to randomly lose upvotes.
        if (other.upvotes < this.upvotes) {
            other.upvotes = this.upvotes;
        }
    }

    public static enum MessageAttribute {
        HIGHLIGHTED,
        RP_ACTION,
        FIRST_TIME_CHATTER,
        ANNOUNCEMENT,
    }

}

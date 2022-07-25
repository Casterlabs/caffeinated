package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonClass(exposeAll = true)
public abstract class MessageMeta extends KoiEvent {

    @JsonField("is_visible")
    private @Setter boolean visible = true;

    private @Setter int upvotes = 0;

    @JsonField("is_highlighted")
    private @Setter boolean isHighlighted = false;

    public void apply(MessageMeta other) {
        other.visible = this.visible;
        other.isHighlighted = this.isHighlighted;

        // Only apply upvotes if we think this instance is current.
        // Sometimes it won't be, and we don't want a message to randomly lose upvotes.
        if (other.upvotes < this.upvotes) {
            other.upvotes = this.upvotes;
        }
    }

}

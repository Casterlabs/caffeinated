package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class MessageReactionEvent extends KoiEvent {
    private @Getter SimpleProfile streamer;

    @JsonField("meta_id")
    private String metaId;

    private String reaction; // Usually just an emoji.

    @Override
    public KoiEventType getType() {
        return KoiEventType.MESSAGE_REACTION;
    }

}

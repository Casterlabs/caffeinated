package co.casterlabs.caffeinated.app.koi.events;

import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppKoiChatUpvoteEvent extends AbstractCancellableEvent<AppKoiEventType> {
    private UserPlatform platform;
    private String messageId;

    public AppKoiChatUpvoteEvent() {
        super(AppKoiEventType.CHAT_UPVOTE);
    }

    @JsonValidate
    private void validate() {
        assert this.platform != null;
        assert this.messageId != null;
    }

}

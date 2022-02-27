package co.casterlabs.caffeinated.app.auth.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppAuthSignoutEvent extends AbstractCancellableEvent<AppAuthEventType> {
    private String tokenId;

    public AppAuthSignoutEvent() {
        super(AppAuthEventType.SIGNOUT);
    }

    @JsonValidate
    private void validate() {
        assert this.tokenId != null;
    }

}

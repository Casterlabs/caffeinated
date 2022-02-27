package co.casterlabs.caffeinated.app.ui.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppUIOpenLinkEvent extends AbstractCancellableEvent<AppUIEventType> {
    private String link;

    public AppUIOpenLinkEvent() {
        super(AppUIEventType.OPENLINK);
    }

    @JsonValidate
    private void validate() {
        assert this.link != null;
    }

}

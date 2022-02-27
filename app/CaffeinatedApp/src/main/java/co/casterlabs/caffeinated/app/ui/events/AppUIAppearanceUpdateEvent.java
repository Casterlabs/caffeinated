package co.casterlabs.caffeinated.app.ui.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppUIAppearanceUpdateEvent extends AbstractCancellableEvent<AppUIEventType> {
    private String theme;
    private String icon;
    private boolean closeToTray;
    private boolean minimizeToTray;

    public AppUIAppearanceUpdateEvent() {
        super(AppUIEventType.APPEARANCE_UPDATE);
    }

    @JsonValidate
    private void validate() {
        assert this.theme != null;
        assert this.icon != null;
    }

}

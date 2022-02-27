package co.casterlabs.caffeinated.app.plugins.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppPluginIntegrationClickWidgetSettingsButtonEvent extends AbstractCancellableEvent<AppPluginIntegrationEventType> {
    private String id;
    private String buttonId;

    public AppPluginIntegrationClickWidgetSettingsButtonEvent() {
        super(AppPluginIntegrationEventType.CLICK_WIDGET_SETTINGS_BUTTON);
    }

    @JsonValidate
    private void validate() {
        assert this.id != null;
        assert this.buttonId != null;
    }

}

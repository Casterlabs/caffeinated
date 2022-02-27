package co.casterlabs.caffeinated.app.plugins.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppPluginIntegrationCopyWidgetUrlEvent extends AbstractCancellableEvent<AppPluginIntegrationEventType> {
    private String id;

    public AppPluginIntegrationCopyWidgetUrlEvent() {
        super(AppPluginIntegrationEventType.COPY_WIDGET_URL);
    }

    @JsonValidate
    private void validate() {
        assert this.id != null;
    }

}

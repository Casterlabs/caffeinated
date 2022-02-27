package co.casterlabs.caffeinated.app.plugins.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppPluginIntegrationDeleteWidgetEvent extends AbstractCancellableEvent<AppPluginIntegrationEventType> {
    private String id;

    public AppPluginIntegrationDeleteWidgetEvent() {
        super(AppPluginIntegrationEventType.DELETE_WIDGET);
    }

    @JsonValidate
    private void validate() {
        assert this.id != null;
    }

}

package co.casterlabs.caffeinated.app.plugins.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppPluginIntegrationCreateWidgetEvent extends AbstractCancellableEvent<AppPluginIntegrationEventType> {
    private String namespace;
    private String name;

    public AppPluginIntegrationCreateWidgetEvent() {
        super(AppPluginIntegrationEventType.CREATE_WIDGET);
    }

    @JsonValidate
    private void validate() {
        assert this.namespace != null;
        assert this.name != null;
    }

}

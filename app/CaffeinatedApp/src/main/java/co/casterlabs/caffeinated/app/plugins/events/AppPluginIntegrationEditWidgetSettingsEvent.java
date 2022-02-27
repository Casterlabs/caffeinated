package co.casterlabs.caffeinated.app.plugins.events;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppPluginIntegrationEditWidgetSettingsEvent extends AbstractCancellableEvent<AppPluginIntegrationEventType> {
    private String id;
    private String key;
    private @Nullable JsonElement newValue;

    public AppPluginIntegrationEditWidgetSettingsEvent() {
        super(AppPluginIntegrationEventType.EDIT_WIDGET_SETTINGS);
    }

    @JsonValidate
    private void validate() {
        assert this.id != null;
        assert this.key != null;
    }

}

package co.casterlabs.caffeinated.pluginsdk.widgets.settings.items;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItemType;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class WidgetSettingsTextBuilder {
    private @With String id;
    /**
     * @deprecated This has been deprecated in favor of the lang system.
     */
    @Deprecated
    private @With String name;

    private @With String defaultValue;

    private @With String placeholder;

    public WidgetSettingsTextBuilder() {
        this.id = null;
        this.name = null;
        this.defaultValue = "";
        this.placeholder = "";
    }

    @SuppressWarnings("deprecation")
    public WidgetSettingsItem build() {
        assert this.id != null : "id cannot be null";
        assert this.defaultValue != null : "defaultValue cannot be null";
        assert this.placeholder != null : "placeholder cannot be null";
        return new WidgetSettingsItem(
            this.id,
            this.name,
            WidgetSettingsItemType.TEXT,
            new JsonObject()
                .put("defaultValue", this.defaultValue)
                .put("placeholder", this.placeholder)
        );
    }

}

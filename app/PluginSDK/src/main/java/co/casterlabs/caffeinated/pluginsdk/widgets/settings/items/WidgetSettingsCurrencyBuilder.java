package co.casterlabs.caffeinated.pluginsdk.widgets.settings.items;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItemType;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class WidgetSettingsCurrencyBuilder {
    private @With String id;
    /**
     * @deprecated This has been deprecated in favor of the lang system.
     */
    @Deprecated
    private @With String name;

    private @With String defaultValue;

    private @With boolean addDefaultOption;

    public WidgetSettingsCurrencyBuilder() {
        this.id = null;
        this.name = null;
        this.defaultValue = null;
        this.addDefaultOption = false;
    }

    @SuppressWarnings("deprecation")
    public WidgetSettingsItem build() {
        assert this.id != null : "id cannot be null";
        assert this.defaultValue != null || this.addDefaultOption : "defaultValue cannot be null IF addDefaultValue is false.";
        return new WidgetSettingsItem(
            this.id,
            this.name,
            WidgetSettingsItemType.CURRENCY,
            new JsonObject()
                .put("defaultValue", this.defaultValue)
                .put("allowDefault", this.addDefaultOption)
        );
    }

}

package co.casterlabs.caffeinated.pluginsdk.widgets.settings.items;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItemType;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class WidgetSettingsRangeBuilder {
    private @With String id;
    /**
     * @deprecated This has been deprecated in favor of the lang system.
     */
    @Deprecated
    private @With String name;

    private @With Number defaultValue;

    private @With Number step;

    private @With Number min;

    private @With Number max;

    public WidgetSettingsRangeBuilder() {
        this.id = null;
        this.name = null;
        this.defaultValue = null;
        this.step = 1;
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
    }

    @SuppressWarnings("deprecation")
    public WidgetSettingsItem build() {
        assert this.id != null : "id cannot be null";
        assert this.defaultValue != null : "defaultValue cannot be null";
        assert this.step != null : "step cannot be null";
        assert this.min != null : "min cannot be null";
        assert this.max != null : "max cannot be null";
        return new WidgetSettingsItem(
            this.id,
            this.name,
            WidgetSettingsItemType.RANGE,
            new JsonObject()
                .put("defaultValue", this.defaultValue)
                .put("step", this.step)
                .put("min", this.min)
                .put("max", this.max)
        );
    }

}

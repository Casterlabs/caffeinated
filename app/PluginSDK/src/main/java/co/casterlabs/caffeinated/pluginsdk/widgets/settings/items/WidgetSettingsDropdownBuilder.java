package co.casterlabs.caffeinated.pluginsdk.widgets.settings.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItemType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class WidgetSettingsDropdownBuilder {
    private @With String id;
    /**
     * @deprecated This has been deprecated in favor of the lang system.
     */
    @Deprecated
    private @With String name;

    private @With String defaultValue;

    private @With Map<String, String> options;

    public WidgetSettingsDropdownBuilder() {
        this.id = null;
        this.name = null;
        this.defaultValue = null;
        this.options = Collections.emptyMap();
    }

    public WidgetSettingsDropdownBuilder withOption(@NonNull String name) {
        return this.withOption(name, name);
    }

    public WidgetSettingsDropdownBuilder withOption(@NonNull String value, @NonNull String name) {
        Map<String, String> clone = new HashMap<>(this.options);
        clone.put(value, name);
        return this.withOptions(Collections.unmodifiableMap(clone));
    }

    public WidgetSettingsDropdownBuilder withOptionsList(@NonNull String... names) {
        Map<String, String> clone = new HashMap<>(this.options);
        for (String name : names) {
            clone.put(name, name);
        }
        return this.withOptions(Collections.unmodifiableMap(clone));
    }

    @SuppressWarnings("deprecation")
    public WidgetSettingsItem build() {
        assert this.id != null : "id cannot be null";
        assert this.defaultValue != null : "defaultValue cannot be null";
        return new WidgetSettingsItem(
            this.id,
            this.name,
            WidgetSettingsItemType.DROPDOWN,
            new JsonObject()
                .put("defaultValue", this.defaultValue)
                .put("options", Rson.DEFAULT.toJson(this.options))
        );
    }

}

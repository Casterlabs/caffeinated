package co.casterlabs.caffeinated.pluginsdk.widgets.settings.items;

import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItemType;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class WidgetSettingsPlatformDropdownBuilder {
    private @With String id;
    /**
     * @deprecated This has been deprecated in favor of the lang system.
     */
    @Deprecated
    private @With String name;

    private @With boolean allowMultiple;

    private @With KoiIntegrationFeatures[] requiredFeatures;

    public WidgetSettingsPlatformDropdownBuilder() {
        this.id = null;
        this.name = null;
        this.allowMultiple = false;
        this.requiredFeatures = null;
    }

    @SuppressWarnings("deprecation")
    public WidgetSettingsItem build() {
        assert this.id != null : "id cannot be null";
        assert this.requiredFeatures != null : "requiredFeatures cannot be null";

        JsonArray defaults = new JsonArray();
        for (Map.Entry<UserPlatform, List<KoiIntegrationFeatures>> features : Caffeinated.getInstance().getKoi().getFeatures().entrySet()) {
            boolean supported = true;
            for (KoiIntegrationFeatures f : this.requiredFeatures) {
                if (!features.getValue().contains(f)) {
                    supported = false;
                    break;
                }
            }
            if (supported) {
                defaults.add(features.getKey().name());
            }
        }

        return new WidgetSettingsItem(
            this.id,
            this.name,
            WidgetSettingsItemType.PLATFORM_DROPDOWN,
            new JsonObject()
                .put("allowMultiple", this.allowMultiple)
                .put("requiredFeatures", Rson.DEFAULT.toJson(this.requiredFeatures))
                .put("defaultValue", defaults)
        );
    }

}

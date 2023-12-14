package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsCheckboxBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsCodeBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsColorBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsCurrencyBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsFileBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsFontBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsNumberBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsPasswordBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsPlatformDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsRangeBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsTextAreaBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsTextBuilder;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
@NonNull
@AllArgsConstructor(onConstructor = @__({
        @Deprecated
}))
@JsonClass(exposeAll = true)
public class WidgetSettingsItem {
    private String id;
    private String name;
    private WidgetSettingsItemType type;
    private JsonObject extraData;

    @SneakyThrows
    public JsonObject getExtraData() {
        return Rson.DEFAULT.fromJson(this.extraData.toString(), JsonObject.class); // Clone
    }

    public @Nullable JsonElement getDefaultValue() {
        return this.getExtraData().get("defaultValue");
    }

    /* ---- Deprecated ---- */

    /**
     * @deprecated Use {@link WidgetSettingsCheckboxBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asCheckbox(@NonNull String id, @Nullable String name, boolean defaultValue) {
        return new WidgetSettingsCheckboxBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .build();
    }

    /* Color */

    /**
     * @deprecated Use {@link WidgetSettingsColorBuilder}.
     */
    @Deprecated
    public static @NonNull WidgetSettingsItem asColor(@NonNull String id, @Nullable String name, @NonNull String defaultValue) {
        return new WidgetSettingsColorBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsNumberBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asNumber(@NonNull String id, @Nullable String name, @NonNull Number defaultValue, @NonNull Number step, @NonNull Number min, @NonNull Number max) {
        return new WidgetSettingsNumberBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withStep(step)
            .withMin(min)
            .withMax(max)
            .build();
    }

    /* Dropdown */

    /**
     * @deprecated Use {@link WidgetSettingsDropdownBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asDropdown(@NonNull String id, @Nullable String name, @NonNull String defaultValue, @NonNull String... options) {
        Map<String, String> map = new HashMap<>();
        for (String option : options) {
            map.put(option, option);
        }
        return asDropdown(id, name, defaultValue, map);
    }

    /**
     * @deprecated Use {@link WidgetSettingsDropdownBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asDropdown(@NonNull String id, @Nullable String name, @NonNull String defaultKey, @NonNull Map<String, String> options) {
        return new WidgetSettingsDropdownBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultKey)
            .withOptions(options)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsTextBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asText(@NonNull String id, @Nullable String name, @NonNull String defaultValue, @NonNull String placeholder) {
        return new WidgetSettingsTextBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withPlaceholder(placeholder)
            .build();
    }

    /* Text Area */

    /**
     * @deprecated Use {@link WidgetSettingsTextAreaBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asTextArea(@NonNull String id, @Nullable String name, @NonNull String defaultValue, @NonNull String placeholder) {
        return asTextArea(id, name, defaultValue, placeholder, 4);
    }

    /**
     * @deprecated Use {@link WidgetSettingsTextAreaBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asTextArea(@NonNull String id, @Nullable String name, @NonNull String defaultValue, @NonNull String placeholder, int rows) {
        return new WidgetSettingsTextAreaBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withPlaceholder(placeholder)
            .withRows(rows)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsCodeBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asCode(@NonNull String id, @Nullable String name, @NonNull String defaultValue, @NonNull String language) {
        return new WidgetSettingsCodeBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withLanguage(language)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsPasswordBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asPassword(@NonNull String id, @Nullable String name, @NonNull String defaultValue, @NonNull String placeholder) {
        return new WidgetSettingsPasswordBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withPlaceholder(placeholder)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsCurrencyBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asCurrency(@NonNull String id, @Nullable String name, @NonNull String defaultValue, boolean allowDefault) {
        return new WidgetSettingsCurrencyBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withAddDefaultOption(allowDefault)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsFontBuilder}.
     */
    @Deprecated
    public static @NonNull WidgetSettingsItem asFont(@NonNull String id, @Nullable String name, @NonNull String defaultValue) {
        return new WidgetSettingsFontBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsRangeBuilder}.
     */
    @Deprecated
    public static WidgetSettingsItem asRange(@NonNull String id, @Nullable String name, @NonNull Number defaultValue, @NonNull Number step, @NonNull Number min, @NonNull Number max) {
        return new WidgetSettingsRangeBuilder()
            .withId(id)
            .withName(name)
            .withDefaultValue(defaultValue)
            .withStep(step)
            .withMin(min)
            .withMax(max)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsFileBuilder}.
     */
    @Deprecated
    public static @NonNull WidgetSettingsItem asFile(@NonNull String id, @Nullable String name, @NonNull String... allowed) {
        return new WidgetSettingsFileBuilder()
            .withId(id)
            .withName(name)
            .withAllowedTypes(allowed)
            .build();
    }

    /**
     * @deprecated Use {@link WidgetSettingsPlatformDropdownBuilder}.
     */
    @Deprecated
    public static @NonNull WidgetSettingsItem asPlatformDropdown(@NonNull String id, @Nullable String name, boolean allowMultiple, @NonNull KoiIntegrationFeatures... requiredFeatures) {
        return new WidgetSettingsPlatformDropdownBuilder()
            .withId(id)
            .withName(name)
            .withAllowMultiple(allowMultiple)
            .withRequiredFeatures(requiredFeatures)
            .build();
    }

}

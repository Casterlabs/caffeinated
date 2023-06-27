package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
@NonNull
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    /* Html */

//    public static WidgetSettingsItem asHtml(@NonNull String html) {
//        return new WidgetSettingsItem(
//            "",
//            "",
//            WidgetSettingsItemType.HTML,
//            new JsonObject()
//                .put("html", html)
//        );
//    }

    /* Checkbox */

    public static WidgetSettingsItem asCheckbox(@NonNull String id, @NonNull String name, boolean defaultValue) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.CHECKBOX,
            new JsonObject()
                .put("defaultValue", defaultValue)
        );
    }

    /* Color */

    public static @NonNull WidgetSettingsItem asColor(@NonNull String id, @NonNull String name, @NonNull String defaultValue) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.COLOR,
            new JsonObject()
                .put("defaultValue", defaultValue)
        );
    }

    /* Number */

    public static WidgetSettingsItem asNumber(@NonNull String id, @NonNull String name, @NonNull Number defaultValue, @NonNull Number step, @NonNull Number min, @NonNull Number max) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.NUMBER,
            new JsonObject()
                .put("defaultValue", defaultValue)
                .put("step", step)
                .put("min", min)
                .put("max", max)
        );
    }

    /* Dropdown */

    /**
     * You may also prefer {@link #asDropdown(String, String, String, Map)}.
     */
    public static WidgetSettingsItem asDropdown(@NonNull String id, @NonNull String name, @NonNull String defaultValue, @NonNull String... options) {
        Map<String, String> map = new HashMap<>();
        for (String option : options) {
            map.put(option, option);
        }
        return asDropdown(id, name, defaultValue, map);
    }

    public static WidgetSettingsItem asDropdown(@NonNull String id, @NonNull String name, @NonNull String defaultKey, @NonNull Map<String, String> options) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.DROPDOWN,
            new JsonObject()
                .put("defaultValue", defaultKey)
                .put("options", Rson.DEFAULT.toJson(options))
        );
    }

    /* Text */

    public static WidgetSettingsItem asText(@NonNull String id, @NonNull String name, @NonNull String defaultValue, @NonNull String placeholder) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.TEXT,
            new JsonObject()
                .put("defaultValue", defaultValue)
                .put("placeholder", placeholder)
        );
    }

    /* Text Area */

    public static WidgetSettingsItem asTextArea(@NonNull String id, @NonNull String name, @NonNull String defaultValue, @NonNull String placeholder) {
        return asTextArea(id, name, defaultValue, placeholder, 4);
    }

    public static WidgetSettingsItem asTextArea(@NonNull String id, @NonNull String name, @NonNull String defaultValue, @NonNull String placeholder, int rows) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.TEXTAREA,
            new JsonObject()
                .put("defaultValue", defaultValue)
                .put("placeholder", placeholder)
                .put("rows", rows)
        );
    }

    /* Password */

    public static WidgetSettingsItem asPassword(@NonNull String id, @NonNull String name, @NonNull String defaultValue, @NonNull String placeholder) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.PASSWORD,
            new JsonObject()
                .put("defaultValue", defaultValue)
                .put("placeholder", placeholder)
        );
    }

    /* Currency */

    public static WidgetSettingsItem asCurrency(@NonNull String id, @NonNull String name, @NonNull String defaultValue, boolean allowDefault) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.CURRENCY,
            new JsonObject()
                .put("defaultValue", defaultValue)
                .put("allowDefault", allowDefault)
        );
    }

    /* Font */

    public static @NonNull WidgetSettingsItem asFont(String id, String name, String defaultValue) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.FONT,
            new JsonObject()
                .put("defaultValue", defaultValue)
        );
    }

    /* Range */

    public static WidgetSettingsItem asRange(@NonNull String id, @NonNull String name, @NonNull Number defaultValue, @NonNull Number step, @NonNull Number min, @NonNull Number max) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.RANGE,
            new JsonObject()
                .put("defaultValue", defaultValue)
                .put("step", step)
                .put("min", min)
                .put("max", max)
        );
    }

    /* File */

    public static @NonNull WidgetSettingsItem asFile(String id, String name, String... allowed) {
        return new WidgetSettingsItem(
            id,
            name,
            WidgetSettingsItemType.FILE,
            new JsonObject()
                .put("allowed", JsonArray.of((Object[]) allowed))
        );
    }

    public static enum WidgetSettingsItemType {
        UNKNOWN,

        CHECKBOX,
        COLOR,
        NUMBER,
        DROPDOWN,
        TEXT,
        TEXTAREA,
        PASSWORD,
        CURRENCY,
        FONT,
        RANGE,
        FILE,

    }

}

package co.casterlabs.caffeinated.pluginsdk.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * A utility class for interacting with your widget's settings (without the
 * hassle)
 * 
 * All setters automatically save the result.
 */
public class WidgetSettings {
    private final Widget widget;

    WidgetSettings(Widget widget) {
        this.widget = widget;
    }

    public boolean has(@NonNull String key) {
        return this.widget.$handle.settings.containsKey(key);
    }

    /* ---------------- */
    /* Getters          */
    /* ---------------- */

    public @Nullable String getString(@NonNull String key) {
        JsonElement e = this.unsafe_get(key);

        if ((e == null) || e.isJsonNull()) {
            return null;
        } else {
            return e.getAsString();
        }
    }

    public @NonNull Number getNumber(@NonNull String key) {
        JsonElement e = this.unsafe_get(key);

        if ((e == null) || e.isJsonNull()) {
            return 0;
        } else {
            return e.getAsNumber();
        }
    }

    public boolean getBoolean(@NonNull String key) {
        JsonElement e = this.unsafe_get(key);

        if ((e == null) || e.isJsonNull()) {
            return false;
        } else {
            return e.getAsBoolean();
        }
    }

    public @Nullable JsonArray getArray(@NonNull String key) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return null;
        } else {
            return e.getAsArray();
        }
    }

    public @Nullable JsonObject getObject(@NonNull String key) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return null;
        } else {
            return e.getAsObject();
        }
    }

    @SneakyThrows
    public @Nullable <T> T get(@NonNull String key, @NonNull Class<T> clazz) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return null;
        } else {
            return Rson.DEFAULT.fromJson(e, clazz);
        }
    }

    @SneakyThrows
    public @Nullable <T> T get(@NonNull String key, @NonNull TypeToken<T> type) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return null;
        } else {
            return Rson.DEFAULT.fromJson(e, type);
        }
    }

    // This needs to copy the settings before accessing
    // as the return types can be mutable.
    public JsonElement get(@NonNull String key) {
        return this.getJson().get(key);
    }

    // Speedup.
    private JsonElement unsafe_get(@NonNull String key) {
        return this.widget.$handle.settings.get(key);
    }

    /* ---------------- */
    /* Getters (def)    */
    /* ---------------- */

    public @NonNull String getString(@NonNull String key, @NonNull String defaultValue) {
        JsonElement e = this.unsafe_get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return e.getAsString();
        }
    }

    public @NonNull Number getNumber(@NonNull String key, @NonNull Number defaultValue) {
        JsonElement e = this.unsafe_get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return e.getAsNumber();
        }
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        JsonElement e = this.unsafe_get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return e.getAsBoolean();
        }
    }

    public @NonNull JsonArray getArray(@NonNull String key, @NonNull JsonArray defaultValue) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return e.getAsArray();
        }
    }

    public @NonNull JsonObject getObject(@NonNull String key, @NonNull JsonObject defaultValue) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return e.getAsObject();
        }
    }

    @SneakyThrows
    public @NonNull <T> T get(@NonNull String key, @NonNull Class<T> clazz, @NonNull T defaultValue) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return Rson.DEFAULT.fromJson(e, clazz);
        }
    }

    @SneakyThrows
    public @NonNull <T> T get(@NonNull String key, @NonNull TypeToken<T> type, @NonNull T defaultValue) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return Rson.DEFAULT.fromJson(e, type);
        }
    }

    // This needs to copy the settings before accessing
    // as the return types can be mutable.
    public @NonNull JsonElement get(@NonNull String key, @NonNull JsonElement defaultValue) {
        JsonElement e = this.get(key);

        if ((e == null) || e.isJsonNull()) {
            return defaultValue;
        } else {
            return e;
        }
    }

    /* ---------------- */
    /* Setters          */
    /* ---------------- */

    public WidgetSettings setNull(@NonNull String key) {
        this.set(key, JsonNull.INSTANCE);
        return this;
    }

    public WidgetSettings set(@NonNull String key, @Nullable Object value) {
        this.setJson(this.getJson().put(key, Rson.DEFAULT.toJson(value)));

        return this;
    }

    /* ---------------- */
    /* "Raw" Access     */
    /* ---------------- */

    /**
     * @return A <b>clone</b> of the underlying settings.
     */
    @SneakyThrows // It will never fail.
    public JsonObject getJson() {
        // Convert to string and then reparse as object,
        // Basically one JANKY clone.
        if (this.widget.$handle.settings == null) {
            return new JsonObject();
        } else {
            return Rson.DEFAULT.fromJson(this.widget.$handle.settings.toString(), JsonObject.class);
        }
    }

    public WidgetSettings setJson(@NonNull JsonObject json) {
        this.widget.setSettings(json);
        return this;
    }

}

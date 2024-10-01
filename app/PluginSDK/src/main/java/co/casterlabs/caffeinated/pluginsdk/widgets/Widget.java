package co.casterlabs.caffeinated.pluginsdk.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.commons.async.promise.Promise;
import co.casterlabs.kaimen.util.reflection.Reflective;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("unchecked") // This is for chaining.
public abstract class Widget {

    public static abstract class WidgetHandle {
        public CaffeinatedPlugin plugin;

        public @JsonField String namespace;
        public @JsonField String id;
        public @JsonField String name; // This is mutable by the end user.
        public @JsonField @Nullable String tag; // This is mutable by the end user.

        public @Reflective Set<KoiEventListener> koiListeners = new HashSet<>();

        public List<WidgetInstance> widgetInstances = new LinkedList<>();
        public Widget widget;

        public @JsonField WidgetDetails details;
        public @JsonField @Nullable WidgetSettingsLayout settingsLayout;

        @NonNull
        @JsonField
        public JsonObject settings = new JsonObject();
        public WidgetSettings widgetSettings;

        public String conductorKey;
        public int conductorPort;

        private String urlFormat = "https://widgets.casterlabs.co/caffeinated/loader.html?pluginId=%s&widgetId=%s&authorization=%s&port=%d&mode=%s";

        public WidgetHandle(Widget w, String conductorKey, int conductorPort) {
            this.widget = w;
            this.conductorKey = conductorKey;
            this.conductorPort = conductorPort;
            this.widgetSettings = new WidgetSettings(this.widget);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.namespace, this.id, this.name, this.details, this.settingsLayout, this.settings);
        }

        public String getUrl() {
            return String.format(
                this.urlFormat,

                this.plugin.getId(),
                this.id,
                this.conductorKey,
                this.conductorPort,
                this.details.getType()
            );
        }

        @JsonSerializationMethod("owner")
        private JsonElement $serialize_owner() {
            return Rson.DEFAULT.toJson(this.plugin.getId());
        }

        @JsonSerializationMethod("url")
        private JsonElement $serialize_url() {
            return Rson.DEFAULT.toJson(this.getUrl());
        }

        @Reflective
        public void cleanlyDestroy() {
            this.widgetInstances.forEach((w) -> {
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            this.widget.onDestroy();
        }

        public void onSettingsUpdate(@Nullable JsonObject newSettings) {
            if (newSettings != null) {
                this.settings = newSettings;
                this.onSettingsUpdate();
                this.widget.onSettingsUpdate();

                for (WidgetInstance widgetInstance : this.widgetInstances) {
                    try {
                        widgetInstance.onSettingsUpdate();
                    } catch (Throwable t) {}
                }
            }
        }

        public abstract void onSettingsUpdate();

    };

    // Package visibility.
    @Reflective
    WidgetHandle $handle;

    @JsonSerializationMethod("_")
    private JsonElement $fail_serialize() {
        throw new RuntimeException("Do not serialize Widget directly, call #toJson() instead.");
    }

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public JsonObject toJson() {
        return Rson.DEFAULT.toJson($handle).getAsObject();
    }

    /* ---------------- */
    /* Framework        */
    /* ---------------- */

    public String getUrl() {
        return $handle.getUrl();
    }

    /**
     * @deprecated While this is used internally, plugins can use it as well for
     *             internal event shenanigans. Though, it is important to note that
     *             it will <b>NOT</b> bubble to the parent plugin or other plugins.
     * 
     * @return     A completion promise, it has no result and is only useful if you
     *             need to ensure the listeners fire before you continue executing.
     *             See {@link Promise#await()} or
     *             {@link Promise#then(java.util.function.Consumer)}
     */
    @Deprecated
    public final Promise<Void> fireKoiEventListeners(@NonNull KoiEvent event) {
        return new Promise<Void>(() -> {
            for (KoiEventListener listener : new ArrayList<>($handle.koiListeners)) {
                try {
                    KoiEventUtil.reflectInvoke(listener, event);
                } catch (Throwable t) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst processing Koi event:");
                    FastLogger.logException(t);
                }
            }

            for (WidgetInstance widgetInstance : this.getWidgetInstances()) {
                try {
                    widgetInstance.onKoiEvent(event);
                } catch (Throwable t) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst processing Koi event:");
                    FastLogger.logException(t);
                }
            }
        });
    }

    /* ---------------- */
    /* Koi              */
    /* ---------------- */

    /**
     * @apiNote Calling {@link #addKoiListener(KoiEventListener)} multiple times
     *          with the same listener won't register it multiple times. The
     *          internal implementation is a {@link HashSet}.
     */
    public final void addKoiListener(@NonNull KoiEventListener listener) {
        $handle.koiListeners.add(listener);
    }

    public final void removeKoiListener(@NonNull KoiEventListener listener) {
        $handle.koiListeners.remove(listener);
    }

    /* ---------------- */
    /* Abstract Methods */
    /* ---------------- */

    public void onInit() {}

    /**
     * @apiNote  By this point the widget will be completely unregistered. You
     *           should speedily destroy whatever you need inorder for your plugin
     *           to properly unload and not leak memory.
     * 
     * @implNote This is called internally, hence the <b>protected</b> modified.
     */
    protected void onDestroy() {}

    public void onNameUpdate() {}

    protected void onSettingsUpdate() {}

    public void onNewInstance(@NonNull WidgetInstance instance) {}

    public abstract @NonNull String getWidgetBasePath(WidgetInstanceMode mode);

    /* ---------------- */
    /* Events           */
    /* ---------------- */

    public void broadcastToAll(@NonNull String type) {
        for (WidgetInstance inst : $handle.widgetInstances) {
            try {
                inst.emit(type);
            } catch (IOException ignored) {}
        }
    }

    public void broadcastToAll(@NonNull String type, @NonNull JsonElement message) {
        for (WidgetInstance inst : $handle.widgetInstances) {
            try {
                inst.emit(type, message);
            } catch (IOException ignored) {}
        }
    }

    public void broadcastToAll(@NonNull String type, @Nullable String message) {
        for (WidgetInstance inst : $handle.widgetInstances) {
            try {
                inst.emit(type, message);
            } catch (IOException ignored) {}
        }
    }

    /* ---------------- */
    /* Mutators         */
    /* ---------------- */

    public final synchronized <T extends Widget> T setSettingsLayout(@NonNull WidgetSettingsLayout newSettingsLayout) {
        JsonObject oldSettings = this.settings().getJson();
        JsonObject newSettings = this.settings().getJson();

        for (WidgetSettingsSection section : newSettingsLayout.getSections()) {
            for (WidgetSettingsItem item : section.getItems()) {
                String key = section.getId() + "." + item.getId();

                if (!oldSettings.containsKey(key)) {
                    JsonElement defaultValue = item.getDefaultValue();
                    if (defaultValue != null) {
                        newSettings.put(key, defaultValue);
                    }
                } else {
                    newSettings.put(key, oldSettings.get(key));
                }
            }
        }

        $handle.settingsLayout = newSettingsLayout;
        $handle.settings = newSettings;
        $handle.onSettingsUpdate();

        return (T) this;
    }

    /**
     * @deprecated the preserveExtraSettings option no longer does anything, use
     *             {@link #setSettingsLayout(WidgetSettingsLayout)} instead.
     */
    @Deprecated
    public final synchronized <T extends Widget> T setSettingsLayout(@NonNull WidgetSettingsLayout newSettingsLayout, boolean preserveExtraSettings) {
        return this.setSettingsLayout(newSettingsLayout);
    }

    /* ---------------- */
    /* Getters          */
    /* ---------------- */

    public final @Nullable WidgetSettingsLayout getSettingsLayout() {
        return $handle.settingsLayout;
    }

    @SneakyThrows
    public final WidgetSettings settings() {
        return $handle.widgetSettings;
    }

    public final <T extends Widget> T setSettings(@NonNull JsonObject newSettings) {
        $handle.settings = newSettings;
        $handle.onSettingsUpdate();

        return (T) this;
    }

    public final String getNamespace() {
        return $handle.namespace;
    }

    public final String getId() {
        return $handle.id;
    }

    public final List<WidgetInstance> getWidgetInstances() {
        return new ArrayList<>($handle.widgetInstances);
    }

    public final WidgetDetails getWidgetDetails() {
        return $handle.details;
    }

    /**
     * @apiNote This name is editable by the end-user. Do <b>NOT</b> treat as a
     *          unique property, use {@link #getId() } for that instead.
     */
    public final String getName() {
        return $handle.name;
    }

    /**
     * The result is auto-cast to whatever type you want.
     */
    public final <T extends CaffeinatedPlugin> T getPlugin() {
        return (T) $handle.plugin;
    }

}

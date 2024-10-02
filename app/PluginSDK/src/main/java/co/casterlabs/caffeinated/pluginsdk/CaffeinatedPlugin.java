package co.casterlabs.caffeinated.pluginsdk;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetSettings;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.commons.async.promise.Promise;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.kaimen.util.reflection.Reflective;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class CaffeinatedPlugin implements Closeable {
    private static @Reflective @Getter boolean devEnvironment;

    private final @Getter FastLogger logger = new FastLogger(this.getName());

    private @Reflective @Nullable ClassLoader classLoader;
    private @Reflective ServiceLoader<Driver> sqlDrivers;

    private @Reflective List<String> widgetNamespaces = new LinkedList<>();
    private @Reflective List<Widget> widgets = new LinkedList<>();

    private @Reflective Set<KoiEventListener> koiListeners = new HashSet<>();

    private @Getter @Deprecated Widget settingsAppletWidget;

    Map<String, Object> services = new HashMap<>();

    /* ---------------- */
    /* Serialization    */
    /* ---------------- */

    @JsonSerializationMethod("version")
    private JsonElement $serialize_version() {
        return Rson.DEFAULT.toJson(this.getVersion());
    }

    @JsonSerializationMethod("author")
    private JsonElement $serialize_author() {
        return Rson.DEFAULT.toJson(this.getAuthor());
    }

    @JsonSerializationMethod("name")
    private JsonElement $serialize_name() {
        return Rson.DEFAULT.toJson(this.getName());
    }

    @JsonSerializationMethod("id")
    private JsonElement $serialize_id() {
        return Rson.DEFAULT.toJson(this.getId());
    }

    @JsonSerializationMethod("lang")
    private JsonElement $serialize_lang() {
        return Rson.DEFAULT.toJson(this.getLang());
    }

    @JsonValidate
    private void validate() throws JsonValidationException {
        throw new JsonValidationException("You cannot deserialize into a CaffeinatedPlugin.");
    }

    /* ---------------- */
    /* Helpers          */
    /* ---------------- */

    /**
     * This handler allows you to override events before the App deals with them.
     * 
     * @implSpec Return true if you wish for the event to be cancelled.
     * @implSpec Return false if the event is fine and should continue (default).
     */
    public boolean shouldCancel(@NonNull KoiEvent event) {
        return false;
    }

    /**
     * @apiNote Calling {@link #addKoiListener(KoiEventListener)} multiple times
     *          with the same listener won't register it multiple times. The
     *          internal implementation is a {@link HashSet}.
     */
    public void addKoiListener(@NonNull KoiEventListener listener) {
        this.koiListeners.add(listener);
    }

    public void removeKoiListener(@NonNull KoiEventListener listener) {
        this.koiListeners.remove(listener);
    }

    /* ---------------- */
    /* Overrides        */
    /* ---------------- */

    /**
     * @apiNote {@link #onInit()} is always called <b>before</b> the plugin is fully
     *          registered.
     */
    public abstract void onInit();

    /**
     * @apiNote {@link #onClose()} is always called <b>after</b> the plugin has been
     *          unregistered.
     */
    public abstract void onClose();

    public @Nullable String getVersion() {
        return null;
    }

    public @Nullable String getAuthor() {
        return null;
    }

    public abstract @NonNull String getName();

    public abstract @NonNull String getId();

    public @Nullable Map<String, LocaleProvider> getLang() {
        return null;
    }

    /* ---------------- */
    /* Getters          */
    /* ---------------- */

    public final @Nullable ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public final List<Widget> getWidgets() {
        return new ArrayList<>(this.widgets);
    }

    /* ---------------- */
    /* Settings & Applets */
    /* ---------------- */

    /**
     * Creates a Settings Applet using the basePath for the UI.
     * 
     * @implNote You cannot have a settings applet that both has a settingsLayout
     *           AND a basePath. You must choose between one or the other.
     */
    protected final void createSettingsApplet(@NonNull String basePath) {
        this.createSettingsApplet(new Widget() {
            @Override
            protected void onSettingsUpdate() {
                CaffeinatedPlugin.this.onSettingsUpdate(); // fwd
            }

            @Override
            public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
                return basePath;
            }
        });
    }

    /**
     * Creates a Settings Applet, use
     * {@link #setSettingsLayout(WidgetSettingsLayout)} to set your settings layout.
     * 
     * @implNote You cannot have a settings applet that both has a settingsLayout
     *           AND a basePath. You must choose between one or the other.
     */
    protected final void createSettingsApplet() {
        this.createSettingsApplet(new Widget() {
            @Override
            protected void onSettingsUpdate() {
                CaffeinatedPlugin.this.onSettingsUpdate(); // fwd
            }

            @Override
            public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
                return "";
            }
        });
    }

    /**
     * @deprecated Use this with caution.
     */
    @Deprecated
    protected final void createSettingsApplet(@NonNull Widget widget) {
        assert this.settingsAppletWidget == null : "You've already create a settings applet for this plugin.";
        WidgetDetails details = new WidgetDetails()
            .withType(WidgetType.SETTINGS_APPLET)
            .withNamespace(this.getId() + ".settings")
            .withFriendlyName(this.getName());
        Caffeinated.getInstance().getPlugins().registerWidgetFactory(this, details, (_ignored) -> widget);
        this.settingsAppletWidget = widget; // It's going to be created already.

        if (!this.settingsAppletWidget.getWidgetBasePath(WidgetInstanceMode.SETTINGS_APPLET).isEmpty()) {
            // Create a dummy layout to signal to the UI that it needs to show a frame
            // instead. Otherwise, a NULL layout indicates that there are no actual settings
            // and the UI should hide the applet from the user (since it's "empty").
            this.settingsAppletWidget.setSettingsLayout(new WidgetSettingsLayout());
        }
    }

    public final WidgetSettings settings() {
        assert this.settingsAppletWidget != null : "You must call createSettingsApplet() inorder to use this feature.";
        return this.settingsAppletWidget.settings();
    }

    public final void setSettings(@NonNull JsonObject newSettings) {
        assert this.settingsAppletWidget != null : "You must call createSettingsApplet() inorder to use this feature.";
        this.settingsAppletWidget.setSettings(newSettings);
    }

    /**
     * @implNote You cannot have a settings applet that both has a settingsLayout
     *           AND a basePath. You must choose between one or the other.
     */
    public final synchronized void setSettingsLayout(@NonNull WidgetSettingsLayout newSettingsLayout) {
        assert this.settingsAppletWidget != null : "You must call createSettingsApplet() inorder to use this feature.";
        assert this.settingsAppletWidget.getWidgetBasePath(WidgetInstanceMode.SETTINGS_APPLET).isEmpty() : "You cannot have a settings applet that both has a settingsLayout AND a basePath. You must choose between one or the other.";
        this.settingsAppletWidget.setSettingsLayout(newSettingsLayout);
    }

    protected void onSettingsUpdate() {}

    /* ---------------- */
    /* Framework        */
    /* ---------------- */

    public void registerService(@NonNull String id, @Nullable Object iface) {
        this.services.put(id, iface);
    }

    /**
     * Use {@link Caffeinated#getPluginIntegration()} instead. Slated for removal
     * soon.
     */
    @Deprecated
    public CaffeinatedPlugins getPlugins() {
        return Caffeinated.getInstance().getPlugins();
    }

    /**
     * @return               A pair of strings, with A being the content and B being
     *                       the content type (nullable)
     * 
     * @implNote             By default, this will read resources directly from your
     *                       jar. You can override this if you need to serve content
     *                       from a custom location.
     * 
     * @throws   IOException
     */
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        InputStream in = this.classLoader.getResourceAsStream(resource);

        String content = StreamUtil.toString(in, StandardCharsets.UTF_8);
        String mime = Caffeinated.getInstance().getMimeForPath(resource);

        return new Pair<>(content, mime);
    }

    /**
     * @deprecated While this is used internally, plugins can use it as well for
     *             internal event shenanigans. Though, it is important to note that
     *             it will <b>NOT</b> bubble to other plugins.
     * 
     * @return     A completion promise, it has no result and is only useful if you
     *             need to ensure the listeners fire before you continue executing.
     *             See {@link Promise#await()} or
     *             {@link Promise#then(java.util.function.Consumer)}
     */
    @Deprecated
    public Promise<Void> fireKoiEventListeners(@NonNull KoiEvent event) {
        return new Promise<Void>(() -> {
            for (KoiEventListener listener : new ArrayList<>(this.koiListeners)) {
                try {
                    KoiEventUtil.reflectInvoke(listener, event);
                } catch (Throwable t) {
                    this.logger.severe("An error occurred whilst processing Koi event:");
                    this.logger.exception(t);
                }
            }

            for (Widget widget : this.getWidgets()) {
                try {
                    widget.fireKoiEventListeners(event).await();
                } catch (Throwable t) {
                    this.logger.severe("An error occurred whilst processing Koi event:");
                    this.logger.exception(t);
                }
            }
        });
    }

    /**
     * @deprecated Do not use, you will destroy your widget on accident. You have
     *             been warned.
     */
    @Deprecated
    @Override
    public final void close() {
        this.onClose();

        // Unload the SQL Drivers.
        for (Driver driver : this.sqlDrivers) {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException ignored) {}
        }
    }

}

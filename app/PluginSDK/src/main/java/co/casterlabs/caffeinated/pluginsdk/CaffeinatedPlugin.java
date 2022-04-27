package co.casterlabs.caffeinated.pluginsdk;

import java.io.Closeable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.kaimen.util.reflection.Reflective;
import co.casterlabs.kaimen.util.threading.Promise;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.types.events.KoiEvent;
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

    // Helpers so the plugin can interract with the framework.
    private @Reflective @Getter CaffeinatedPlugins plugins;

    private @Reflective @Nullable ClassLoader classLoader;
    private @Reflective ServiceLoader<Driver> sqlDrivers;

    private @Reflective List<String> widgetNamespaces = new LinkedList<>();
    private @Reflective List<Widget> widgets = new LinkedList<>();

    private @Reflective Set<KoiEventListener> koiListeners = new HashSet<>();

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

    /**
     * A helper to get resources out of the plugin to a widget.
     * 
     * @implNote            plugins should override this and return whatever data
     *                      they want.
     * 
     * @param    resourceId the id of the resource
     * 
     * @return              null if no data is found.
     */
    public @Nullable byte[] getResource(@NonNull String resourceId) {
        return null;
    }

    public @Nullable JsonObject getLang() {
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
    /* Framework        */
    /* ---------------- */

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
    @SuppressWarnings("deprecation")
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

            return null;
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

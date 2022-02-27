package co.casterlabs.caffeinated.app.plugins.impl;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugins;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.util.Producer;
import co.casterlabs.caffeinated.util.Triple;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class PluginsHandler implements CaffeinatedPlugins {
    private static final FastLogger logger = new FastLogger();

    private Map<String, CaffeinatedPlugin> plugins = new HashMap<>();
    private Map<String, Triple<CaffeinatedPlugin, Producer<Widget>, WidgetDetails>> widgetFactories = new HashMap<>();
    private Map<String, WidgetHandle> widgetHandles = new HashMap<>();

    public List<CaffeinatedPlugin> getPlugins() {
        return new ArrayList<>(this.plugins.values());
    }

    public List<WidgetHandle> getWidgetHandles() {
        return new ArrayList<>(this.widgetHandles.values());
    }

    public WidgetHandle getWidgetHandle(@NonNull String id) {
        return this.widgetHandles.get(id);
    }

    public List<WidgetDetails> getCreatableWidgets() {
        List<WidgetDetails> details = new LinkedList<>();

        for (Triple<CaffeinatedPlugin, Producer<Widget>, WidgetDetails> factory : this.widgetFactories.values()) {
            details.add(factory.c);
        }

        return details;
    }

    /* ---------------- */
    /* Other Methods    */
    /* ---------------- */

    public WidgetHandle createApplet(@NonNull String namespace, @Nullable JsonObject settings) {
        return this.createWidget(namespace, namespace + ".applet", "Applet", settings, WidgetType.APPLET);
    }

    public WidgetHandle createDock(@NonNull String namespace, @Nullable JsonObject settings) {
        return this.createWidget(namespace, namespace + ".dock", "Dock", settings, WidgetType.DOCK);
    }

    public WidgetHandle createWidget(@NonNull String namespace, @NonNull String id, @NonNull String name, @Nullable JsonObject settings) {
        return this.createWidget(namespace, id, name, settings, WidgetType.WIDGET);
    }

    @SneakyThrows
    private WidgetHandle createWidget(@NonNull String namespace, @NonNull String id, @NonNull String name, @Nullable JsonObject settings, @NonNull WidgetType expectedType) {
        Triple<CaffeinatedPlugin, Producer<Widget>, WidgetDetails> factory = this.widgetFactories.get(namespace);

        assert factory != null : "A factory associated to that widget is not registered.";
        assert factory.c.getType() == expectedType : "That widget is not of the expected type of " + expectedType;

        List<Widget> pluginWidgetsField = ReflectionLib.getValue(factory.a, "widgets");

        String conductorKey = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorKey();
        int conductorPort = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorPort();

        WidgetHandle handle = new WidgetHandle(factory.b.produce(), conductorKey, conductorPort) {
            @Override
            public void onSettingsUpdate() {
                CaffeinatedApp.getInstance().getPlugins().save();
            }
        };

        ReflectionLib.setValue(handle.widget, "$handle", handle);

        handle.namespace = namespace;
        handle.id = id;
        handle.name = name;
        handle.plugin = factory.a;
        handle.details = factory.c;

        // Register it, update it, and return it.
        this.widgetHandles.put(handle.widget.getId(), handle);
        pluginWidgetsField.add(handle.widget);

        new AsyncTask(() -> {
            if (settings != null) {
                handle.settings = settings;
            }

            handle.widget.onInit();
            handle.widget.onNameUpdate();
        });

        return handle;
    }

    @SneakyThrows
    public void destroyWidget(@NonNull String id) {
        WidgetHandle handle = this.widgetHandles.remove(id);

        assert handle != null : "That widget is not registered.";

        List<Widget> pluginWidgetsField = ReflectionLib.getValue(handle.plugin, "widgets");

        pluginWidgetsField.remove(handle.widget);

        handle.cleanlyDestroy();
    }

    /* ---------------- */
    /* Override Methods */
    /* ---------------- */

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends CaffeinatedPlugin> T getPluginById(@NonNull String id) {
        CaffeinatedPlugin pl = this.plugins.get(id);

        if (pl == null) {
            return null;
        } else {
            return (T) pl;
        }
    }

    @Override
    public boolean isPluginPresent(@NonNull String id) {
        return this.plugins.containsKey(id);
    }

    @SneakyThrows
    @Override
    public CaffeinatedPlugins registerWidgetFactory(@NonNull CaffeinatedPlugin plugin, @NonNull WidgetDetails widgetDetails, @NonNull Producer<Widget> widgetProducer) {
        assert !this.widgetFactories.containsKey(widgetDetails.getNamespace()) : "A widget of that namespace is already registered.";

        widgetDetails.validate();

        List<String> pluginWidgetNamespacesField = ReflectionLib.getValue(plugin, "widgetNamespaces");
        pluginWidgetNamespacesField.add(widgetDetails.getNamespace());

        this.widgetFactories.put(widgetDetails.getNamespace(), new Triple<>(plugin, widgetProducer, widgetDetails));

        // Automatically create the docks and applets when registered.
        switch (widgetDetails.getType()) {
            case APPLET:
                this.createApplet(widgetDetails.getNamespace(), null);
                break;

            case DOCK:
                this.createDock(widgetDetails.getNamespace(), null);
                break;

            case WIDGET:
            default:
                break;

        }

        return this;
    }

    /* ---------------- */
    /* Loading Methods  */
    /* ---------------- */

    public PluginContext loadPluginsFromClassLoader(@NonNull ClassLoader loader) {
        try {
            List<CaffeinatedPlugin> toLoad = PluginLoader.loadFromClassLoader(this, loader);

            return unsafe_loadPlugins(toLoad, loader.toString());
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", loader);
            logger.exception(e);
            return new PluginContext(Collections.emptyList(), false);
        }
    }

    public PluginContext loadPluginsFromFile(@NonNull File file) throws Exception {
        try {
            List<CaffeinatedPlugin> toLoad = PluginLoader.loadFile(this, file);
            PluginContext ctx = unsafe_loadPlugins(toLoad, file.getName());

            ctx.setFile(file);

            return ctx;
        } catch (Exception e) {
            throw e;
        }
    }

    public PluginContext unsafe_loadPlugins(List<CaffeinatedPlugin> toLoad, String source) {
        List<String> pluginIds = new LinkedList<>();
        boolean hasSucceeded = false;

        try {
            for (CaffeinatedPlugin plugin : toLoad) {
                pluginIds.add(plugin.getId());
                this.registerPlugin(plugin);
            }

            hasSucceeded = true;
            logger.info("Loaded all plugins from %s successfully.", source);
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", source);
            logger.exception(e);

            for (String id : new ArrayList<>(pluginIds)) {
                try {
                    this.unregisterPlugin(id);
                    pluginIds.remove(id);
                } catch (Throwable ignored) {}
            }
        }

        return new PluginContext(pluginIds, hasSucceeded);
    }

    public void unregisterAll() {
        for (String id : this.plugins.keySet().toArray(new String[0])) {
            this.unregisterPlugin(id);
        }
    }

    /* ---------------- */
    /* Manual registration */
    /* ---------------- */

    public void registerPlugin(@NonNull CaffeinatedPlugin plugin) {
        String id = plugin.getId();

        if (this.plugins.containsKey(id)) {
            logger.warn("A plugin with an id of '%s' is already registered.", plugin.getId());
        } else {
            logger.info("Loaded plugin %s (%s)", plugin.getName(), id);
            plugin.onInit();
            this.plugins.put(id, plugin);
        }
    }

    @SneakyThrows
    @SuppressWarnings("deprecation")
    public void unregisterPlugin(@NonNull String id) {
        assert this.plugins.containsKey(id) : id + " is not registered.";

        CaffeinatedPlugin plugin = this.plugins.remove(id);
        ClassLoader classLoader = plugin.getClassLoader();
        List<Widget> pluginWidgetsField = ReflectionLib.getValue(plugin, "widgets");
        List<String> pluginWidgetNamespacesField = ReflectionLib.getValue(plugin, "widgetNamespaces");

        for (String widgetNamespace : pluginWidgetNamespacesField) {
            this.widgetFactories.remove(widgetNamespace);
        }

        for (Widget widget : new ArrayList<>(pluginWidgetsField)) {
            this.destroyWidget(widget.getId());
        }

        try {
            plugin.close();
        } catch (Throwable ignored) {}

        try {
            if (classLoader instanceof Closeable) {
                ((Closeable) classLoader).close();
            }
        } catch (Throwable ignored) {}

        logger.info("Unloaded plugin %s (%s)", plugin.getName(), id);

        // Important for the GC sweep to remove the class loader.
        plugin = null;
        classLoader = null;

        System.gc();
    }

}

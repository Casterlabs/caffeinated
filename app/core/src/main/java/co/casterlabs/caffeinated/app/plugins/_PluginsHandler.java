package co.casterlabs.caffeinated.app.plugins;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.window.AppWindow;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

class _PluginsHandler {
    private static final FastLogger logger = new FastLogger();

    static Map<String, Triple<CaffeinatedPlugin, Function<WidgetDetails, Widget>, WidgetDetails>> widgetFactories = new HashMap<>();

    static final Map<String, CaffeinatedPlugin> plugins = new HashMap<>();
    static final Map<String, WidgetHandle> widgetHandles = new HashMap<>();
    static final Collection<WidgetDetails> creatableWidgets = new LinkedList<>();

    static List<WidgetDetails> getCreatableWidgets() {
        List<WidgetDetails> details = new LinkedList<>();

        for (Triple<CaffeinatedPlugin, Function<WidgetDetails, Widget>, WidgetDetails> factory : widgetFactories.values()) {
            details.add(factory.c());
        }

        return details;
    }

    static WidgetHandle getWidgetHandle(@NonNull String id) {
        return widgetHandles.get(id);
    }

    /* ---------------- */
    /* Other Methods    */
    /* ---------------- */

    static WidgetHandle createApplet(@NonNull String namespace, @Nullable JsonObject settings) {
        return createWidget(namespace, namespace + ".applet", "Applet", settings, WidgetType.APPLET);
    }

    static WidgetHandle createSettingsApplet(@NonNull String namespace, @Nullable JsonObject settings) {
        return createWidget(namespace, namespace + ".settings_applet", "Settings Applet", settings, WidgetType.SETTINGS_APPLET);
    }

    static WidgetHandle createDock(@NonNull String namespace, @Nullable JsonObject settings) {
        return createWidget(namespace, namespace + ".dock", "Dock", settings, WidgetType.DOCK);
    }

    static WidgetHandle createWidget(@NonNull String namespace, @NonNull String id, @NonNull String name, @Nullable JsonObject settings) {
        return createWidget(namespace, id, name, settings, WidgetType.WIDGET);
    }

    @SneakyThrows
    private static WidgetHandle createWidget(@NonNull String namespace, @NonNull String id, @NonNull String name, @Nullable JsonObject settings, @NonNull WidgetType expectedType) {
        Triple<CaffeinatedPlugin, Function<WidgetDetails, Widget>, WidgetDetails> factory = widgetFactories.get(namespace);

        assert factory != null : "A factory associated to that widget is not registered.";
        assert factory.c().getType() == expectedType : "That widget is not of the expected type of " + expectedType;

        List<Widget> pluginWidgetsField = ReflectionLib.getValue(factory.a(), "widgets");

        for (Widget widget : pluginWidgetsField) {
            if (widget.getId().equals(id)) {
                assert false : "That widget is already registered.";
            }
        }

        String conductorKey = AppConfig.appPreferences.get().conductorKey;
        int conductorPort = AppConfig.appPreferences.get().conductorPort();

        WidgetHandle handle = new WidgetHandle(factory.b().apply(factory.c()), conductorKey, conductorPort) {
            @SuppressWarnings("deprecation")
            @Override
            public void onSettingsUpdate() {
                AppWindow.INSTANCE.emit(
                    "widgets:" + this.id,
                    this.widget.toJson()
                );
                AppPlugins.save(this);
            }
        };

        ReflectionLib.setValue(handle.widget, "$handle", handle);

        handle.namespace = namespace;
        handle.id = id;
        handle.name = name;
        handle.plugin = factory.a();
        handle.details = factory.c();

        // Register it, update it, and return it.
        widgetHandles.put(handle.widget.getId(), handle);
        pluginWidgetsField.add(handle.widget);

        AsyncTask.create(() -> {
            // Set the settings.
            if (settings != null) {
                handle.settings = settings;
            }

            handle.widget.onInit();
            handle.widget.onNameUpdate();
            handle.onSettingsUpdate(settings); // Call an update.
        });

        return handle;
    }

    @SneakyThrows
    static void destroyWidget(@NonNull String id) {
        WidgetHandle handle = widgetHandles.remove(id);
        assert handle != null : "That widget is not registered.";

        List<Widget> pluginWidgetsField = ReflectionLib.getValue(handle.plugin, "widgets");

        pluginWidgetsField.remove(handle.widget);

        handle.cleanlyDestroy();
    }

    /* ---------------- */
    /* Loading Methods  */
    /* ---------------- */

    static _PluginContext loadPluginsFromClassLoader(@NonNull ClassLoader loader) {
        try {
            List<CaffeinatedPlugin> toLoad = _PluginLoader.loadFromClassLoader(loader);

            return unsafe_loadPlugins(toLoad, loader.toString());
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", loader);
            logger.exception(e);
            return new _PluginContext(Collections.emptyList(), false);
        }
    }

    static _PluginContext loadPluginsFromFile(@NonNull File file) throws Exception {
        try {
            List<CaffeinatedPlugin> toLoad = _PluginLoader.loadFile(file);
            _PluginContext ctx = unsafe_loadPlugins(toLoad, file.getName());

            ctx.setFile(file);

            return ctx;
        } catch (Exception e) {
            throw e;
        }
    }

    static _PluginContext unsafe_loadPlugins(List<CaffeinatedPlugin> toLoad, String source) {
        List<String> pluginIds = new LinkedList<>();
        boolean hasSucceeded = false;

        try {
            for (CaffeinatedPlugin plugin : toLoad) {
                pluginIds.add(plugin.getId());
                registerPlugin(plugin);
            }

            hasSucceeded = true;
            logger.info("Loaded all plugins from %s successfully.", source);
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", source);
            logger.exception(e);

            for (String id : new ArrayList<>(pluginIds)) {
                try {
                    unregisterPlugin(id);
                    pluginIds.remove(id);
                } catch (Throwable ignored) {}
            }
        }

        return new _PluginContext(pluginIds, hasSucceeded);
    }

    static void unregisterAll() {
        for (String id : plugins.keySet().toArray(new String[0])) {
            unregisterPlugin(id);
        }
    }

    private static void registerPlugin(@NonNull CaffeinatedPlugin plugin) {
        String id = plugin.getId();

        if (plugins.containsKey(id)) {
            logger.warn("A plugin with an id of '%s' is already registered.", plugin.getId());
        } else {
            logger.info("Loaded plugin %s (%s)", plugin.getName(), id);
            plugin.onInit();
            plugins.put(id, plugin);
        }
    }

    @SneakyThrows
    @SuppressWarnings("deprecation")
    static void unregisterPlugin(@NonNull String id) {
        assert plugins.containsKey(id) : id + " is not registered.";

        CaffeinatedPlugin plugin = plugins.remove(id);
        ClassLoader classLoader = plugin.getClassLoader();
        List<Widget> pluginWidgetsField = ReflectionLib.getValue(plugin, "widgets");
        List<String> pluginWidgetNamespacesField = ReflectionLib.getValue(plugin, "widgetNamespaces");

        for (String widgetNamespace : pluginWidgetNamespacesField) {
            Triple<CaffeinatedPlugin, Function<WidgetDetails, Widget>, WidgetDetails> removed = widgetFactories.remove(widgetNamespace);
            creatableWidgets.remove(removed.c());
        }

        for (Widget widget : new ArrayList<>(pluginWidgetsField)) {
            try {
                destroyWidget(widget.getId());
            } catch (Throwable t) {
                logger.warn("An error occurred whilst destroying widget for unload:\n%s", t);
            }
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

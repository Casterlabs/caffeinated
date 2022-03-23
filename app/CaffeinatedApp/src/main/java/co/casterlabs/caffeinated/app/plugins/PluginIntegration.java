package co.casterlabs.caffeinated.app.plugins;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.plugins.PluginContext.ContextType;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences.WidgetSettingsDetails;
import co.casterlabs.caffeinated.app.ui.UIDocksPlugin;
import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptGetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class PluginIntegration extends JavascriptObject {
    private static final File pluginsDir = new File(CaffeinatedApp.appDataDir, "plugins");

    private PluginsHandler plugins = new PluginsHandler();
    private @JavascriptValue(allowSet = false, watchForMutate = true) List<PluginContext> contexts = new ArrayList<>();

    private boolean isLoading = true;

    // Pointers to forward values from PluginsHandler.

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private final Collection<CaffeinatedPlugin> loadedPlugins = this.plugins.$loadedPlugins;
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private final Collection<WidgetDetails> creatableWidgets = this.plugins.$creatableWidgets;
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private final Collection<WidgetHandle> widgets = this.plugins.$widgetHandles;

    public PluginIntegration() {
        pluginsDir.mkdir();
    }

    @SneakyThrows
    public void init() {
        // Load the built-in widgets.
        {
            CaffeinatedPlugin defaultPlugin = new CaffeinatedDefaultPlugin();
            ReflectionLib.setValue(defaultPlugin, "plugins", this.plugins);
            PluginContext ctx = this.plugins.unsafe_loadPlugins(Arrays.asList(defaultPlugin), "Caffeinated");
            ctx.setPluginType(ContextType.INTERNAL);
            this.contexts.add(ctx);
        }

        // Load the UI Docks
        {
            CaffeinatedPlugin uiDocksPlugin = new UIDocksPlugin();

            ReflectionLib.setValue(uiDocksPlugin, "plugins", this.plugins);
            PluginContext ctx = this.plugins.unsafe_loadPlugins(Arrays.asList(uiDocksPlugin), "Caffeinated");
            ctx.setPluginType(ContextType.INTERNAL);
            this.contexts.add(ctx);
        }

        for (File file : pluginsDir.listFiles()) {
            String fileName = file.getName();

            if (file.isFile() &&
                fileName.endsWith(".jar") &&
                !fileName.startsWith("__")) {
                this.loadFile(file);
            }
        }

        for (WidgetSettingsDetails details : CaffeinatedApp.getInstance().getPluginIntegrationPreferences().get().getWidgetSettings()) {
            try {
                String id = details.getId();

                // Reconstruct the widget and ignore the applet and dock.
                if (!id.contains("applet") && !id.contains("dock")) {
                    this.plugins.createWidget(details.getNamespace(), id, details.getName(), details.getSettings());
                }
            } catch (AssertionError | SecurityException | NullPointerException | IllegalArgumentException e) {
                if ("That widget is not of the expected type of WIDGET".equals(e.getMessage())) {
                    continue; // Ignore.
                } else if ("A factory associated to that widget is not registered.".equals(e.getMessage())) {
                    // We can safely ignore it.
                    // TODO let the user know that the widget could not be found.
                    FastLogger.logStatic(LogLevel.WARNING, "Unable to create missing widget: %s (%s)", details.getName(), details.getNamespace());
                } else {
                    e.printStackTrace();
                }
            }
        }

        this.isLoading = false;
        this.save();
    }

    public void save() {
        if (!this.isLoading) {
            PreferenceFile<PluginIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getPluginIntegrationPreferences();

            List<WidgetSettingsDetails> widgetSettings = new LinkedList<>();
            for (WidgetHandle handle : this.plugins.getWidgetHandles()) {
                widgetSettings.add(WidgetSettingsDetails.from(handle.widget));
            }

            prefs.get().setWidgetSettings(widgetSettings);
            prefs.save();
        }
    }

    @SneakyThrows
    @JavascriptFunction
    public void openPluginsDir() {
        Desktop.getDesktop().browse(pluginsDir.toURI());
    }

    @JavascriptFunction
    public List<String> listFiles() throws Exception {
        List<File> files = new LinkedList<>(
            Arrays.asList(
                pluginsDir.listFiles()
            )
        );

        // Remove the files that belong to already loaded contexts.
        for (PluginContext ctx : this.contexts) {
            if (ctx.getFile() != null) {
                files.remove(ctx.getFile());
            }
        }

        // Grab the file names.
        List<String> names = new ArrayList<>(files.size());

        files.forEach(f -> names.add(f.getName()));

        return names;
    }

    @JavascriptFunction
    public void load(@NonNull String file) throws Exception {
        this.loadFile(new File(pluginsDir, file));
    }

    private void loadFile(@NonNull File file) {
        try {
            this.contexts.add(
                this.plugins.loadPluginsFromFile(file)
            );

            FastLogger.logStatic(LogLevel.INFO, "Loaded %s", file.getName());
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to load %s as a plugin, make sure that it's *actually* a plugin!", file.getName());
            FastLogger.logException(e);
        }
    }

    @JavascriptFunction
    public void unload(@NonNull String ctxId) {
        PluginContext ctx = null;

        for (PluginContext c : this.contexts) {
            if (c.getId().equals(ctxId)) {
                ctx = c;
                break;
            }
        }

        assert ctx != null;
        assert ctx.getPluginType() == ContextType.PLUGIN : "You cannot unload this plugin.";

        for (String id : ctx.getPluginIds()) {
            this.plugins.unregisterPlugin(id);
        }

        this.contexts.remove(ctx);
    }

    @JavascriptFunction
    public void createNewWidget(@NonNull String namespace, @NonNull String name) {
        WidgetHandle handle = this.plugins.createWidget(namespace, UUID.randomUUID().toString(), name, null);

        this.save();
        CaffeinatedApp.getInstance().getUI().navigate("/pages/edit-widget?widget=" + handle.id);
    }

    @SneakyThrows
    @JavascriptFunction
    public void renameWidget(@NonNull String widgetId, @NonNull String newName) {
        WidgetHandle handle = this.plugins.getWidgetHandle(widgetId);

        handle.name = newName;
        this.save();

        handle.widget.onNameUpdate();
    }

    @JavascriptFunction
    public void deleteWidget(@NonNull String widgetId) {
        this.plugins.destroyWidget(widgetId);
        this.save();
    }

    @JavascriptFunction
    public void editWidgetSettingsItem(@NonNull String widgetId, @NonNull String key, @Nullable JsonElement value) {
        WidgetHandle handle = this.plugins.getWidgetHandle(widgetId);

        JsonObject settings = handle.settings;

        // JsonNull should always be converted to null.
        if ((value == null) || value.isJsonNull()) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }

        handle.onSettingsUpdate(settings);

        this.save();
    }

    @JavascriptFunction
    public void clickWidgetSettingsButton(@NonNull String widgetId, @NonNull String buttonId) {
        WidgetHandle handle = this.plugins.getWidgetHandle(widgetId);

        for (WidgetSettingsButton b : handle.settingsLayout.getButtons()) {
            if (b.getId().equals(buttonId)) {
                new AsyncTask(b.getOnClick());
                return;
            }
        }
    }

    @JavascriptFunction
    public void copyWidgetUrl(@NonNull String widgetId) {
        WidgetHandle handle = this.plugins.getWidgetHandle(widgetId);

        CaffeinatedApp.getInstance().copyText(handle.getUrl(), null);
    }

    @JavascriptGetter("loadedPlugins")
    public List<CaffeinatedPlugin> getLoadedPlugins() {
        return this.plugins.getPlugins();
    }

    @JavascriptGetter("creatableWidgets")
    public List<WidgetDetails> getCreatableWidgets() {
        return this.plugins.getCreatableWidgets();
    }

    @JavascriptGetter("widgets")
    public List<WidgetHandle> getWidgetHandles() {
        return this.plugins.getWidgetHandles();
    }

}

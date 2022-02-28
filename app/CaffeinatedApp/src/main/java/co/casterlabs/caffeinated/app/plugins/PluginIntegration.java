package co.casterlabs.caffeinated.app.plugins;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences.WidgetSettingsDetails;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationClickWidgetSettingsButtonEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationCopyWidgetUrlEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationCreateWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationDeleteWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationEditWidgetSettingsEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationEventType;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationRenameWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.impl.PluginContext;
import co.casterlabs.caffeinated.app.plugins.impl.PluginsHandler;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.UIBackgroundColor;
import co.casterlabs.caffeinated.app.ui.UIDocksPlugin;
import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.util.ClipboardUtil;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.BridgeValue;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class PluginIntegration {
    private static EventHandler<AppPluginIntegrationEventType> handler = new EventHandler<>();
    private static final File pluginsDir = new File(CaffeinatedApp.appDataDir, "plugins");

    private PluginsHandler plugins = new PluginsHandler();
    private List<PluginContext> contexts = new ArrayList<>();

    private BridgeValue<PluginsBridgeObject> bridge = new BridgeValue<>("plugins", new PluginsBridgeObject());

    @SuppressWarnings("unused")
    @JsonClass(exposeAll = true)
    private static class PluginsBridgeObject {
        private List<CaffeinatedPlugin> loadedPlugins;
        private List<WidgetDetails> creatableWidgets;
        private List<WidgetHandle> widgets;

    }

    public PluginIntegration() {
        handler.register(this);

        pluginsDir.mkdir();
    }

    @SneakyThrows
    public void init() {
        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.bridge);

        // Load the built-in widgets.
        {
            CaffeinatedPlugin defaultPlugin = new CaffeinatedDefaultPlugin();

            ReflectionLib.setValue(defaultPlugin, "plugins", this.plugins);
            this.contexts.add(this.plugins.unsafe_loadPlugins(Arrays.asList(defaultPlugin), "Caffeinated"));
        }

        {
            CaffeinatedPlugin uiDocksPlugin = new UIDocksPlugin();

            ReflectionLib.setValue(uiDocksPlugin, "plugins", this.plugins);
            this.contexts.add(this.plugins.unsafe_loadPlugins(Arrays.asList(uiDocksPlugin), "Caffeinated"));
        }

        for (File file : pluginsDir.listFiles()) {
            String fileName = file.getName();

            if (file.isFile() &&
                fileName.endsWith(".jar") &&
                !fileName.startsWith("__")) {
                try {
                    this.contexts.add(
                        this.plugins.loadPluginsFromFile(file)
                    );
                    FastLogger.logStatic(LogLevel.INFO, "Loaded %s", fileName);
                } catch (Exception e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "Unable to load %s as a plugin, make sure that it's *actually* a plugin!", fileName);
                    FastLogger.logException(e);
                }
            }
        }

        for (WidgetSettingsDetails details : CaffeinatedApp.getInstance().getPluginIntegrationPreferences().get().getWidgetSettings()) {
            try {
                String id = details.getId();

                // Reconstruct the widget and ignore the applet and dock.
                if (!id.equals("applet") && !id.equals("dock")) {
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

        this.save();
    }

    public void save() {
        PreferenceFile<PluginIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getPluginIntegrationPreferences();

        List<WidgetSettingsDetails> widgetSettings = new LinkedList<>();
        for (WidgetHandle handle : this.plugins.getWidgetHandles()) {
            widgetSettings.add(WidgetSettingsDetails.from(handle.widget));
        }

        prefs.get().setWidgetSettings(widgetSettings);
        prefs.save();

        this.updateBridgeData();
    }

    @EventListener
    public void onPluginIntegrationCreateWidgetEvent(AppPluginIntegrationCreateWidgetEvent event) {
        WidgetHandle handle = this.plugins.createWidget(event.getNamespace(), UUID.randomUUID().toString(), event.getName(), null);

        this.save();
        CaffeinatedApp.getInstance().getUI().navigate("/pages/edit-widget?widget=" + handle.id);
    }

    @SneakyThrows
    @EventListener
    public void onPluginIntegrationRenameWidgetEvent(AppPluginIntegrationRenameWidgetEvent event) {
        WidgetHandle handle = this.plugins.getWidgetHandle(event.getId());

        handle.name = event.getNewName();
        this.save();

        handle.widget.onNameUpdate();
    }

    @EventListener
    public void onPluginIntegrationDeleteWidgetEvent(AppPluginIntegrationDeleteWidgetEvent event) {
        this.plugins.destroyWidget(event.getId());

        this.save();
    }

    @EventListener
    public void onPluginIntegrationEditWidgetSettingsEvent(AppPluginIntegrationEditWidgetSettingsEvent event) {
        WidgetHandle handle = this.plugins.getWidgetHandle(event.getId());

        JsonObject settings = handle.settings;
        JsonElement value = event.getNewValue();

        // JsonNull should always be converted to null.
        if ((value == null) || value.isJsonNull()) {
            settings.remove(event.getKey());
        } else {
            settings.put(event.getKey(), value);
        }

        handle.onSettingsUpdate(settings);

        this.save();
    }

    @EventListener
    public void onPluginIntegrationClickSettingsButtonEvent(AppPluginIntegrationClickWidgetSettingsButtonEvent event) {
        WidgetHandle handle = this.plugins.getWidgetHandle(event.getId());

        WidgetSettingsButton button = null;

        for (WidgetSettingsButton b : handle.settingsLayout.getButtons()) {
            if (b.getId().equals(event.getButtonId())) {
                button = b;
                break;
            }
        }

        if (button != null) {
            new AsyncTask(button.getOnClick());
        }
    }

    @EventListener
    public void onPluginIntegrationCopyWidgetUrlEvent(AppPluginIntegrationCopyWidgetUrlEvent event) {
        WidgetHandle handle = this.plugins.getWidgetHandle(event.getId());

        ClipboardUtil.copy(handle.getUrl());

        CaffeinatedApp.getInstance().getUI().showToast("Copied link to clipboard", UIBackgroundColor.PRIMARY);
    }

    public void updateBridgeData() {
        this.bridge.get().loadedPlugins = this.plugins.getPlugins();
        this.bridge.get().creatableWidgets = this.plugins.getCreatableWidgets();
        this.bridge.get().widgets = this.plugins.getWidgetHandles();
        this.bridge.update();
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppPluginIntegrationEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}

package co.casterlabs.caffeinated.app.plugins;

import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.builtins.BuiltIns;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.plugins._PluginContext.ContextType;
import co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl;
import co.casterlabs.caffeinated.app.sdk.KoiImpl;
import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.koi.TestEvents;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.util.network.InterfaceUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.emoji.generator.WebUtil;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.yen.Cache;
import co.casterlabs.yen.CacheIterator;
import co.casterlabs.yen.impl.SQLBackedCache;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@JavascriptObject
public class AppPlugins {
    private static final File pluginsDir = new File(AppConfig.APP_DATA_DIR, "plugins");
    private static final String addressesStringList = String.join(",", InterfaceUtil.getLocalIpAddresses());

    private static @Getter Cache<WidgetSettingsDetails> preferenceData;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static Set<_PluginContext> contexts = new HashSet<>();

    // Pointers to forward values from PluginsHandler.
    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static final Collection<CaffeinatedPlugin> loadedPlugins = _PluginsHandler.plugins.values();
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static final Collection<WidgetHandle> widgets = _PluginsHandler.widgetHandles.values();
    @JavascriptValue(allowSet = false)
    private static final Collection<WidgetDetails> creatableWidgets = _PluginsHandler.creatableWidgets;

    static {
        pluginsDir.mkdir();
    }

    @SneakyThrows
    public static void init() {
        preferenceData = new SQLBackedCache<>(-1, AppConfig.preferencesConnection, "kv_plugins");

        // Migrate from the old format to the new KV.
        _PluginImporter.importOldJson().forEach(preferenceData::submit);

        // Load the built-in widgets.
        {
            CaffeinatedPlugin defaultPlugin = new CaffeinatedDefaultPlugin();
            _PluginContext ctx = _PluginsHandler.unsafe_loadPlugins(Arrays.asList(defaultPlugin), "Caffeinated");
            ctx.setPluginType(ContextType.INTERNAL);
            contexts.add(ctx);
        }

        // Load the built-ins
        for (CaffeinatedPlugin service : BuiltIns.init()) {
            _PluginContext ctx = _PluginsHandler.unsafe_loadPlugins(Arrays.asList(service), "Caffeinated");
            ctx.setPluginType(ContextType.INTERNAL);
            contexts.add(ctx);
        }

        for (File file : pluginsDir.listFiles()) {
            String fileName = file.getName();

            if (file.isFile() &&
                fileName.endsWith(".jar") &&
                !fileName.startsWith("__")) {
                loadFile(file);
            }
        }

        // Load all widgets.
        try (CacheIterator<WidgetSettingsDetails> it = preferenceData.enumerate()) {
            while (it.hasNext()) {
                WidgetSettingsDetails details = it.next();

                try {
                    String id = details.getId();

                    // Reconstruct the widget and ignore the applet and dock.
                    if (!id.contains("applet") && !id.contains("dock")) {
                        _PluginsHandler.createWidget(details.getNamespace(), id, details.getName(), details.getSettings());
                    }
                } catch (AssertionError | SecurityException | NullPointerException | IllegalArgumentException e) {
                    if ("That widget is not of the expected type of WIDGET".equals(e.getMessage()) ||
                        "That widget is already registered.".equals(e.getMessage())) {
                        continue; // Ignore.
                    } else if ("A factory associated to that widget is not registered.".equals(e.getMessage())) {
                        // We can safely ignore it.
                        // TODO let the user know that the widget could not be found.
                        FastLogger.logStatic(LogLevel.WARNING, "Unable to create missing widget: %s (%s)", details.getName(), details.getNamespace());
                        FastLogger.logStatic(LogLevel.WARNING, "Note that this widget will NOT be deleted from the database, it will persist until the user reinstalls the plugin and deletes it themselves.");
                    } else {
                        e.printStackTrace();
                    }
                }

                // Okay, we've produced a LOT of garbage after doing all of that.
                // Let's see if we can bring our apparent usage down :^)
                System.gc();
            }
        }

        widgets.forEach(AppPlugins::save);
    }

    public static void save(WidgetHandle handle) {
        preferenceData.submit(WidgetSettingsDetails.from(handle.widget));
    }

    @SneakyThrows
    @JavascriptFunction
    public static void openPluginsDir() {
        Desktop.getDesktop().browse(pluginsDir.toURI());
    }

    @JavascriptFunction
    public static List<String> listFiles() throws Exception {
        return Arrays.asList(pluginsDir.listFiles())
            .parallelStream()
            .filter((file) -> {
                // Remove the files that belong to already loaded contexts.
                for (_PluginContext ctx : contexts) {
                    if (ctx.getFile() != null && ctx.getFile().equals(file)) {
                        return false;
                    }
                }
                return true;
            })
            .map((file) -> file.getName()) // Get the name of the files.
            .collect(Collectors.toList());
    }

    @JavascriptFunction
    public static void load(@NonNull String file) throws Exception {
        loadFile(new File(pluginsDir, file));
    }

    private static void loadFile(@NonNull File file) {
        try {
            contexts.add(
                _PluginsHandler.loadPluginsFromFile(file)
            );

            System.gc();
            FastLogger.logStatic(LogLevel.INFO, "Loaded %s", file.getName());
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to load %s as a plugin, make sure that it's *actually* a plugin!", file.getName());
            FastLogger.logException(e);
        }
    }

    @JavascriptFunction
    public static void unload(@NonNull String ctxId) {
        _PluginContext ctx = null;

        for (_PluginContext c : contexts) {
            if (c.getId().equals(ctxId)) {
                ctx = c;
                break;
            }
        }

        assert ctx != null;
        assert ctx.getPluginType() == ContextType.PLUGIN : "You cannot unload this plugin.";

        contexts.remove(ctx);

        for (String id : ctx.getPluginIds()) {
            _PluginsHandler.unregisterPlugin(id);
        }
    }

    @JavascriptFunction
    public static String createNewWidget(@NonNull String namespace, @NonNull String name) {
        WidgetHandle handle = _PluginsHandler.createWidget(namespace, UUID.randomUUID().toString(), name, null);

        try {
            handle.onSettingsUpdate(new JsonObject());
        } catch (Throwable ignored) {} // Some widgets get mad. Whatever.

        save(handle);

        return handle.id;
    }

    @JavascriptFunction
    public static void renameWidget(@NonNull String widgetId, @NonNull String newName) {
        WidgetHandle handle = _PluginsHandler.getWidgetHandle(widgetId);

        handle.name = newName;
        save(handle);

        handle.widget.onNameUpdate();
    }

    @JavascriptFunction
    public static void assignTag(@NonNull String widgetId, @Nullable String tagOrNull) {
        WidgetHandle handle = _PluginsHandler.getWidgetHandle(widgetId);

        handle.tag = tagOrNull;
        save(handle);
    }

    @JavascriptFunction
    public static void deleteWidget(@NonNull String widgetId) {
        _PluginsHandler.destroyWidget(widgetId);
        preferenceData.remove(widgetId);
    }

    @JavascriptFunction
    public static void editWidgetSettingsItem(@NonNull String widgetId, @NonNull String key, @Nullable JsonElement value) {
        WidgetHandle handle = _PluginsHandler.getWidgetHandle(widgetId);

        JsonObject settings = handle.settings;

        // JsonNull should always be converted to null.
        if ((value == null) || value.isJsonNull()) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }

        handle.onSettingsUpdate(settings);

        save(handle);
    }

    @SuppressWarnings("deprecation")
    @JavascriptFunction
    public static void fireTestEvent(@NonNull String widgetId, @NonNull KoiEventType type) {
        WidgetHandle handle = _PluginsHandler.getWidgetHandle(widgetId);

        // Pick a random account that we're signed-in to.
        UserUpdateEvent[] userStates = KoiImpl.INSTANCE.getUserStates().values().toArray(new UserUpdateEvent[0]);
        UserUpdateEvent randomAccount = userStates[ThreadLocalRandom.current().nextInt(userStates.length)];

        KoiEvent event = TestEvents.createTestEvent(type, randomAccount.streamer.platform);
        handle.widget.fireKoiEventListeners(event);
    }

    @JavascriptFunction
    public static void clickWidgetSettingsButton(@NonNull String widgetId, @NonNull String buttonId) {
        WidgetHandle handle = _PluginsHandler.getWidgetHandle(widgetId);

        for (WidgetSettingsButton b : handle.settingsLayout.getButtons()) {
            if (b.getId().equals(buttonId)) {
                AsyncTask.create(b.getOnClick());
                return;
            }
        }
    }

    @JavascriptFunction
    public static void copyWidgetUrl(@NonNull String widgetId) {
        WidgetHandle handle = _PluginsHandler.getWidgetHandle(widgetId);
        String url = handle.getUrl();
        url += "&addresses=";
        url += WebUtil.encodeURIComponent(addressesStringList);

        CaffeinatedImpl.INSTANCE.copyText(url, "Copied link to clipboard");
    }

    @JavascriptFunction
    public static void openPopout(@NonNull String widgetId) {
//        SaucerApp.dispatch(() -> {
//            Saucer saucer = Saucer.create(Bootstrap.getPreferences());
//
//            saucer.window().setTitle("Casterlabs-Caffeinated");
//            saucer.bridge().defineObject("Caffeinated", CaffeinatedApp);
//            saucer.webview().setContextMenuAllowed(false);
//
//            String appUrl = Bootstrap.getAppUrl() + "/popout/new-window?id=" + widgetId;
//            saucer.webview().setSchemeHandler(AppSchemeHandler.INSTANCE);
//            saucer.webview().setUrl(appUrl);
//
//            saucer.webview().setDevtoolsVisible(true);
//            saucer.window().setMinSize(new SaucerSize(200, 200));
//
//            saucer.window().setAlwaysOnTop(true);
//            // TODO icon.
//
//            saucer.window().show();
//        });
    }

}

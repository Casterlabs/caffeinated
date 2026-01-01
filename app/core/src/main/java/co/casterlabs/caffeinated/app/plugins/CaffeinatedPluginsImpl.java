package co.casterlabs.caffeinated.app.plugins;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugins;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.commons.functional.tuples.Triple;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.yen.CacheIterator;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class CaffeinatedPluginsImpl implements CaffeinatedPlugins {
    public static final CaffeinatedPluginsImpl INSTANCE = new CaffeinatedPluginsImpl();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CaffeinatedPlugin> @Nullable T getPluginById(@NonNull String id) {
        CaffeinatedPlugin pl = _PluginsHandler.plugins.get(id);

        if (pl == null) {
            return null;
        } else {
            return (T) pl;
        }
    }

    @Override
    public boolean isPluginPresent(@NonNull String id) {
        return _PluginsHandler.plugins.containsKey(id);
    }

    @SneakyThrows
    @Override
    public CaffeinatedPlugins registerWidgetFactory(@NonNull CaffeinatedPlugin plugin, @NonNull WidgetDetails widgetDetails, @NonNull Function<WidgetDetails, Widget> widgetFactory) {
        assert !_PluginsHandler.widgetFactories.containsKey(widgetDetails.getNamespace()) : "A widget of that namespace is already registered.";

        widgetDetails.validate();

        List<String> pluginWidgetNamespacesField = ReflectionLib.getValue(plugin, "widgetNamespaces");
        pluginWidgetNamespacesField.add(widgetDetails.getNamespace());

        _PluginsHandler.widgetFactories.put(widgetDetails.getNamespace(), new Triple<>(plugin, widgetFactory, widgetDetails));
        _PluginsHandler.creatableWidgets.add(widgetDetails);

        // Automatically create the docks and applets when registered.
        if ((widgetDetails.getType() == WidgetType.DOCK) || (widgetDetails.getType() == WidgetType.APPLET) || (widgetDetails.getType() == WidgetType.SETTINGS_APPLET)) {
            JsonObject settings = null;

            // Now we need to find the widget's settings (if they exist)
            try (CacheIterator<WidgetSettingsDetails> it = AppPlugins.getPreferenceData().enumerate()) {
                while (it.hasNext()) {
                    WidgetSettingsDetails otherDetails = it.next();

                    if (otherDetails.getNamespace().equals(widgetDetails.getNamespace())) {
                        settings = otherDetails.getSettings();
                        break;
                    }
                }
            }

            switch (widgetDetails.getType()) {
                case APPLET:
                    _PluginsHandler.createApplet(widgetDetails.getNamespace(), settings);
                    break;

                case SETTINGS_APPLET:
                    _PluginsHandler.createSettingsApplet(widgetDetails.getNamespace(), settings);
                    break;

                case DOCK:
                    _PluginsHandler.createDock(widgetDetails.getNamespace(), settings);
                    break;

                default:
                    break;
            }
        }

        return this;
    }

}

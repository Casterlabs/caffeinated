package co.casterlabs.caffeinated.pluginsdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public interface CaffeinatedPlugins {

    /**
     * @param  pluginId  The plugin which has registered the service.
     * @param  serviceId The service you wish to bind to the interface.
     * @param  iface     The interface class.
     * 
     * @return           An instance of the provided interface.
     */
    @SuppressWarnings("unchecked")
    default <T> T bindInterfaceToService(@NonNull String pluginId, @NonNull String serviceId, @NonNull Class<T> iface) {
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class<?>[] {
                    iface
            },
            (Object proxy, Method method, Object[] args) -> {
                return this.callServiceMethod(pluginId, serviceId, method.getName(), args);
            }
        );
    }

    @SneakyThrows
    default <T> T callServiceMethod(@NonNull String pluginId, @NonNull String serviceId, @NonNull String methodName, @Nullable Object... args) {
        if (args == null) args = new Object[0];

        CaffeinatedPlugin plugin = this.getPluginById(pluginId);
        assert plugin != null : "The specified plugin cannot be found";
        Object service = plugin.services.get(serviceId);
        assert service != null : "The specified service cannot be found";

        FastLogger.logStatic(LogLevel.DEBUG, "callServiceMethod -> %s(%s)", methodName, args);
        return ReflectionLib.invokeMethod(service, methodName, args);
    }

    /**
     * Gets a plugin by it's id. The result is autocast to your type of choice.
     *
     * @param  <T> the generic type
     * @param  id  the id
     * 
     * @return     the plugin, null if not loaded
     */
    public @Nullable <T extends CaffeinatedPlugin> T getPluginById(@NonNull String id);

    /**
     * Detects whether or not a plugin is loaded.
     *
     * @param  id the id
     * 
     * @return    true if loaded
     */
    public boolean isPluginPresent(@NonNull String id);

    /**
     * Registers a widget that can be created by the user.
     *
     * @param  plugin        the plugin registering the widget
     * @param  widgetDetails the details of the widget
     * @param  widgetfactory the factory that creates the widget instances
     * 
     * @return               this instance, for chaining.
     */
    public CaffeinatedPlugins registerWidgetFactory(@NonNull CaffeinatedPlugin plugin, @NonNull WidgetDetails widgetDetails, @NonNull Function<WidgetDetails, Widget> widgetFactory);

    /**
     * Registers a widget that can be created by the user.
     * 
     * @implNote               This method just creates a producer and uses
     *                         reflection to call a no-args constructor on the class
     *                         to return an instance. Even though widgets should
     *                         <b>NOT</b> use the constructor for initialization,
     *                         those that wish should use a producer and register
     *                         their widget with
     *                         {@link CaffeinatedPlugins#registerWidgetFactory(CaffeinatedPlugin, String, Producer)}
     *                         instead.
     * 
     * @param    plugin        the plugin registering the widget
     * @param    widgetDetails the details of the widget
     * @param    widgetClass   the widget class of the widget, see the implNote for
     *                         extra info
     * 
     * 
     * @return                 this instance, for chaining
     */
    default CaffeinatedPlugins registerWidget(@NonNull CaffeinatedPlugin plugin, @NonNull WidgetDetails widgetDetails, @NonNull Class<? extends Widget> widgetClass) {
        return this.registerWidgetFactory(plugin, widgetDetails, (_details) -> {
            try {
                // Try to use the no-args constructor.
                return widgetClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e); // Ugh.
            }
        });
    }

}

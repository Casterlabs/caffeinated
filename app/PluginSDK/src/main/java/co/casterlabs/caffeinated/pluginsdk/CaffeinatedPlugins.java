package co.casterlabs.caffeinated.pluginsdk;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import lombok.NonNull;

public interface CaffeinatedPlugins {

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
     * @param  plugin         the plugin registering the widget
     * @param  widgetDetails  the details of the widget
     * @param  widgetProducer the {@code Producer} factory
     * 
     * @return                this instance, for chaining.
     */
    public CaffeinatedPlugins registerWidgetFactory(@NonNull CaffeinatedPlugin plugin, @NonNull WidgetDetails widgetDetails, @NonNull Supplier<Widget> widgetProducer);

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
        return this.registerWidgetFactory(plugin, widgetDetails, () -> {
            try {
                // Try to use the no-args constructor.
                return widgetClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e); // Ugh.
            }
        });
    }

}

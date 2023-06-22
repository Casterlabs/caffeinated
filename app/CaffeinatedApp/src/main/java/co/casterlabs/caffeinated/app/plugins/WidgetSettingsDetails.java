package co.casterlabs.caffeinated.app.plugins;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.yen.Cacheable;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class WidgetSettingsDetails implements Cacheable {
    private String id;
    private String namespace;
    private String name;
    private JsonObject settings = new JsonObject();

    @Override
    public String id() {
        return this.id;
    }

    public static WidgetSettingsDetails from(@NonNull Widget widget) {
        WidgetSettingsDetails details = new WidgetSettingsDetails();

        details.id = widget.getId();
        details.namespace = widget.getNamespace();
        details.name = widget.getName();
        details.settings = widget.settings().getJson();

        return details;
    }

}

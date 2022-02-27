package co.casterlabs.caffeinated.pluginsdk.widgets;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

/**
 * See {@link https://feathericons.com/} for icons.
 */
@Value
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class WidgetDetails {
    private @With String namespace;

    private @With String icon;
    private @With String friendlyName;
    private @With WidgetDetailsCategory category;

    private @With WidgetType type;

    private @With(AccessLevel.PRIVATE) boolean showDemo;
    private @With(AccessLevel.PRIVATE) double demoAspectRatio;

    public WidgetDetails() {
        this.namespace = null;
        this.icon = "grid";
        this.friendlyName = null;
        this.category = WidgetDetailsCategory.OTHER;
        this.type = WidgetType.WIDGET;
        this.showDemo = false;
        this.demoAspectRatio = 0;
    }

    public WidgetDetails withShowDemo(boolean showDemo, double aspectRatio) {
        return this.withShowDemo(showDemo)
            .withDemoAspectRatio(aspectRatio);
    }

    public void validate() {
        assert this.namespace != null : "Namespace cannot be null";
        assert this.icon != null : "Icon cannot be null";
        assert this.friendlyName != null : "Friendly Name cannot be null";
    }

    public static enum WidgetDetailsCategory {
        ALERTS,
        LABELS,
        INTERACTION,
        GOALS,
        OTHER;

    }

}

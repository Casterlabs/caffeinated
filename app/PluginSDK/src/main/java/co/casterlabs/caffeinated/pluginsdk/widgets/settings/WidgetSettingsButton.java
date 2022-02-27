package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

/**
 * See {@link https://feathericons.com/} for icons.
 */
@Value
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class WidgetSettingsButton {
    private String id;

    private @With String icon;
    private @With String iconTitle;

    private @With String text;

    private @JsonExclude @With Runnable onClick;

    public WidgetSettingsButton(@NonNull String id) {
        this.id = id;
        this.icon = null;
        this.iconTitle = null;
        this.text = null;
        this.onClick = null;
    }

    public void validate() {
        assert this.id != null : "Id cannot be null.";
        assert (this.id != null) || (this.text != null) : "Button must have either an icon or text, it cannot be blank.";
        assert this.onClick != null : "onClick cannot be null.";
    }

}

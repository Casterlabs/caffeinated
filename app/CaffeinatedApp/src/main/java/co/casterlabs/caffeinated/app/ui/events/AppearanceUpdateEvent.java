package co.casterlabs.caffeinated.app.ui.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class AppearanceUpdateEvent {
    private String theme;
    private String icon;
    private String emojiProvider;
    private boolean closeToTray;
    private boolean mikeysMode;

    @JsonValidate
    private void validate() {
        assert this.theme != null;
        assert this.icon != null;
        assert this.emojiProvider != null;
    }

}

package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class AppearanceUpdateEvent {
    private String icon;
    private String emojiProvider;
    private String language;
    private boolean closeToTray;
    private boolean enableStupidlyUnsafeSettings;
    private boolean enableAlternateThemes;
    private boolean mikeysMode;

    @JsonValidate
    private void validate() {
        assert this.icon != null;
        assert this.emojiProvider != null;
        assert this.language != null;
    }

}

package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.saucer.bridge.JavascriptObject;
import lombok.Data;

@Data
@JavascriptObject
@JsonClass(exposeAll = true)
public class ThemePreferences {
    private String baseColor = "mauve";
    private String primaryColor = "crimson";
    private Appearance appearance = Appearance.DARK;

    public static enum Appearance {
        FOLLOW_SYSTEM,
        LIGHT,
        DARK
    }

}

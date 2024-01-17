package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class ThemePreferences {
    private String baseColor = "mauve";
    private String primaryColor = "crimson";
    private Appearance appearance = Appearance.DARK;

    public static enum Appearance {
        DARK,
        LIGHT,
        FOLLOW_SYSTEM
    }

}

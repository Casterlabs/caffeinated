package co.casterlabs.caffeinated.app.ui;

import com.jthemedetecor.OsThemeDetector;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.ThemePreferences.Appearance;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;
import lombok.Getter;
import lombok.NonNull;

@JavascriptObject
public class ThemeManager {
    private PreferenceFile<ThemePreferences> preferenceFile = new PreferenceFile<>("theme", ThemePreferences.class);

    // These all include some defaults so that the app can load and not look
    // completely broken.
    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private String baseColor = "gray";

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private String primaryColor = "gray";

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Appearance appearance = Appearance.FOLLOW_SYSTEM;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private @Getter Appearance effectiveAppearance = Appearance.LIGHT; // Calculated.

    private Appearance systemAppearance = Appearance.LIGHT;
    private OsThemeDetector systemAppearanceDetector;

    public void init() {
        this.systemAppearanceDetector = OsThemeDetector.getDetector();

        this.baseColor = this.preferenceFile.get().getBaseColor();
        this.primaryColor = this.preferenceFile.get().getPrimaryColor();
        this.appearance = this.preferenceFile.get().getAppearance();

        this.systemAppearanceDetector.registerListener(isDark -> {
            this.systemAppearance = isDark ? Appearance.DARK : Appearance.LIGHT;
            this.calculateEffectiveTheme();
        });

        this.systemAppearance = this.systemAppearanceDetector.isDark() ? Appearance.DARK : Appearance.LIGHT;
        this.calculateEffectiveTheme();
    }

    private void calculateEffectiveTheme() {
        if (this.appearance == Appearance.FOLLOW_SYSTEM) {
            this.effectiveAppearance = this.systemAppearance;
        } else {
            this.effectiveAppearance = this.appearance;
        }

        CaffeinatedApp.getInstance().getSaucer().webview().setForceDarkAppearance(this.effectiveAppearance == Appearance.DARK);
    }

    @JavascriptFunction
    public void setTheme(@NonNull String baseColor, @NonNull String primaryColor, @NonNull Appearance appearance) {
        this.preferenceFile.get().setBaseColor(baseColor);
        this.preferenceFile.get().setPrimaryColor(primaryColor);
        this.preferenceFile.get().setAppearance(appearance);
        this.preferenceFile.save();

        this.appearance = appearance;
        this.baseColor = baseColor;
        this.primaryColor = primaryColor;

        this.calculateEffectiveTheme();
    }

}

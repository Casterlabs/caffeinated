package co.casterlabs.caffeinated.app.ui;

import com.jthemedetecor.OsThemeDetector;

import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.AppWindow;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.ui.ThemePreferences.Appearance;
import lombok.Getter;

@JavascriptObject
public class AppThemeManager {
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static @Getter Appearance effectiveAppearance = Appearance.LIGHT; // Calculated.

    private static Appearance systemAppearance = Appearance.LIGHT;
    private static OsThemeDetector systemAppearanceDetector;

    public static void init() {
        systemAppearanceDetector = OsThemeDetector.getDetector();

        systemAppearanceDetector.registerListener(isDark -> {
            systemAppearance = isDark ? Appearance.DARK : Appearance.LIGHT;
            calculateEffectiveTheme();
        });

        systemAppearance = systemAppearanceDetector.isDark() ? Appearance.DARK : Appearance.LIGHT;
        calculateEffectiveTheme();
    }

    private static void calculateEffectiveTheme() {
        if (AppConfig.themePreferences.get().getAppearance() == Appearance.FOLLOW_SYSTEM) {
            effectiveAppearance = systemAppearance;
        } else {
            effectiveAppearance = AppConfig.themePreferences.get().getAppearance();
        }

        AppWindow.forceDarkEnabled(effectiveAppearance == Appearance.DARK);
    }

    public static void onUpdatePreferences() {
        calculateEffectiveTheme();
        AppUI.onUpdatePreferences();
    }

}

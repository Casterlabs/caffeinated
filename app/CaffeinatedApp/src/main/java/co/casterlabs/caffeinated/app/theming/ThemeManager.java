package co.casterlabs.caffeinated.app.theming;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.App.Appearance;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class ThemeManager extends JavascriptObject {
    private static final String FALLBACK = "SYSTEM";
    private static final String EFF_LIGHT = "CASTERLABS_LIGHT";
    private static final String EFF_DARK = "CASTERLABS_DARK";

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<String, Theme> themes = new HashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private @Getter Theme currentTheme;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private @Getter Theme effectiveTheme;

    public void init() {
        this.registerTheme(
            new Theme("SYSTEM", Appearance.FOLLOW_SYSTEM, true),
            new Theme("CASTERLABS_LIGHT", Appearance.LIGHT, false),
            new Theme("CASTERLABS_DARK", Appearance.DARK, false)
        );

        String theme = CaffeinatedApp.getInstance().getUI().getPreferences().getTheme();

        try {
            setTheme(theme);
        } catch (Throwable e) {
            FastLogger.logStatic("Falling back onto default theme, error: %s", e.getMessage());
            setTheme(FALLBACK);
        }

        App.systemThemeChangeEvent.on(this::calculateEffectiveTheme);
    }

    private void calculateEffectiveTheme(Appearance appearance) {
        if (!this.currentTheme.isAuto()) {
            this.effectiveTheme = this.currentTheme;
            return;
        }

        if (appearance == Appearance.LIGHT) {
            this.effectiveTheme = this.themes.get(EFF_LIGHT);
        } else {
            this.effectiveTheme = this.themes.get(EFF_DARK);
        }
    }

    public void registerTheme(@NonNull Theme... themes) {
        for (Theme theme : themes) {
            this.themes.put(theme.getId(), theme);
        }
    }

    @JavascriptSetter("theme")
    public void setTheme(@NonNull String id) {
        if ((this.currentTheme != null) && this.currentTheme.getId().equals(id)) {
            return;
        }

        Theme theme = this.themes.get(id);
        assert theme != null : "There is no theme registered with an id of '" + id + "'";

        this.currentTheme = theme;

        try {
            App.setAppearance(theme.getAppearance());
            this.calculateEffectiveTheme(theme.getAppearance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

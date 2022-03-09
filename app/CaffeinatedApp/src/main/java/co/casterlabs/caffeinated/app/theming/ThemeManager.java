package co.casterlabs.caffeinated.app.theming;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.App.Appearance;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import lombok.Getter;
import lombok.NonNull;

public class ThemeManager {

    @JavascriptValue(allowSet = false)
    private Map<String, Theme> themes = new HashMap<>();

    @JavascriptValue(allowSet = false)
    private @Getter Theme currentTheme;

    public void init() {
        this.registerTheme(
            new Theme("system", "Follow System", Appearance.FOLLOW_SYSTEM, true),
            new Theme("co.casterlabs.light", "Light", Appearance.LIGHT, false),
            new Theme("co.casterlabs.dark", "Dark", Appearance.DARK, false)
        );

        setTheme(CaffeinatedApp.getInstance().getUI().getPreferences().getTheme());
    }

    public void registerTheme(@NonNull Theme... themes) {
        for (Theme theme : themes) {
            this.themes.put(theme.getId(), theme);
        }
    }

    @JavascriptSetter("theme")
    public void setTheme(@NonNull String id) {
        if ((currentTheme != null) && currentTheme.getId().equals(id)) {
            return;
        }

        final String defaultTheme = "system";

        Theme theme = themes.get(id);

        if (theme == null) {
            theme = themes.get(defaultTheme);
        }

        assert theme != null : "There is no theme registered with an id of '" + id + "' or '" + defaultTheme + "'";

        currentTheme = theme;

        App.setAppearance(theme.getAppearance());
    }

}

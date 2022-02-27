package co.casterlabs.caffeinated.app.theming;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.App.Appearance;
import co.casterlabs.kaimen.webview.bridge.BridgeValue;
import lombok.Getter;
import lombok.NonNull;

public class ThemeManager {

    private static Map<String, Theme> themes = new HashMap<>();
    private static @Getter Theme currentTheme;

    private static BridgeValue<Theme> bridge_Theme = new BridgeValue<>("theme");
    private static BridgeValue<Collection<Theme>> bridge_Themes = new BridgeValue<>("themes");

    public static void init() {
        // Light theme
        ThemeManager.registerTheme(
            new Theme("co.casterlabs.light", "Light")
                .withCssFiles(false, "/css/bulma.min.css")
        );

        // Dark theme
        ThemeManager.registerTheme(
            new Theme("co.casterlabs.dark", "Dark")
                .withCssFiles(false, "/css/bulma.min.css", "/css/bulma-prefers-dark.min.css")
                .withClasses("bulma-dark-mode")
                .withDark(true)
        );

        setTheme(CaffeinatedApp.getInstance().getUiPreferences().get().getTheme(), "co.casterlabs.dark");

        CaffeinatedApp.getInstance().getAppBridge().attachValue(bridge_Theme);
        CaffeinatedApp.getInstance().getAppBridge().attachValue(bridge_Themes);
    }

    public static void registerTheme(@NonNull Theme theme) {
        themes.put(theme.getId(), theme);
        bridge_Themes.set(themes.values());
    }

    public static void setTheme(@NonNull String id, @NonNull String defaultTheme) {
        Theme theme = themes.get(id);

        if (theme == null) {
            theme = themes.get(defaultTheme);
        }

        assert theme != null : "There is no theme registered with an id of '" + id + "' or '" + defaultTheme + "'";

        currentTheme = theme;
        bridge_Theme.set(theme);

        App.setAppearance(theme.isDark() ? Appearance.DARK : Appearance.LIGHT);
    }

}

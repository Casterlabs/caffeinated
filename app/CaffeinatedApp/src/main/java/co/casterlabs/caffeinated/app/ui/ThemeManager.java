package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.App.Appearance;
import co.casterlabs.kaimen.app.AppEvent;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import lombok.Getter;
import lombok.NonNull;

public class ThemeManager extends JavascriptObject {
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

    // Calculated.
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private @Getter Appearance effectiveAppearance;

    public void init() {
        this.baseColor = this.preferenceFile.get().getBaseColor();
        this.primaryColor = this.preferenceFile.get().getPrimaryColor();
        this.appearance = this.preferenceFile.get().getAppearance();

        App.on(AppEvent.APPEARANCE_CHANGE, this::calculateEffectiveTheme);
        this.calculateEffectiveTheme();
    }

    private void calculateEffectiveTheme() {
        if (this.appearance == Appearance.FOLLOW_SYSTEM) {
            App.setAppearance(Appearance.FOLLOW_SYSTEM);
            Appearance appearance = App.getAppearance();
            this.effectiveAppearance = appearance;
        } else {
            App.setAppearance(this.appearance);
            this.effectiveAppearance = this.appearance;
        }
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

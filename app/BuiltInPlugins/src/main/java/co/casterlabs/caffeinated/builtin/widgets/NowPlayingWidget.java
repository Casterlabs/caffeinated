package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import lombok.NonNull;

public class NowPlayingWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.now_playing")
        .withIcon("musical-note")
        .withCategory(WidgetDetailsCategory.OTHER)
        .withFriendlyName("Now Playing Widget")
        .withShowDemo(true, 64 / 199d);

    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("style", "Style")
                .addItem(WidgetSettingsItem.asCheckbox("visible", "Is Visible", true))
                .addItem(WidgetSettingsItem.asDropdown("background_style", "Background Style", "Blur", "Blur", "Clear", "Solid"))
                .addItem(WidgetSettingsItem.asDropdown("image_style", "Image Location", "Left", "Left", "Right", "None"))
        );

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/now-playing.html";
    }

}

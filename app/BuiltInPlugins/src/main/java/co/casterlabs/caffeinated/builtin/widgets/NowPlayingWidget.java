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
        .withShowDemo(true, 260 / 670d);

    @Override
    public void onInit() {
        this.generateSettingsLayout();
    }

    @Override
    protected void onSettingsUpdate() {
        this.generateSettingsLayout();
    }

    protected void generateSettingsLayout() {
        WidgetSettingsSection section = new WidgetSettingsSection("style", "Style")
            .addItem(WidgetSettingsItem.asDropdown("card_style", "Card Style", "Horizontal Card", "Horizontal Card", "Text Only", "Image Only"));

        switch (this.settings().getString("style.card_style", "Horizontal Card")) {
            case "Horizontal Card": {
                section
                    .addItem(WidgetSettingsItem.asDropdown("background_style", "Background Style", "Blur", "Blur", "Clear", "Solid"))
                    .addItem(WidgetSettingsItem.asDropdown("image_style", "Image Location", "Left", "Left", "Right", "None"));
                break;
            }

            case "Text Only": {
                section
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                    .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ff0000"))
                    .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"));
                break;
            }

            case "Image Only": {
                break;
            }

        }

        this.setSettingsLayout(
            new WidgetSettingsLayout()
                .addSection(section)
        );
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/now-playing.html";
    }

}

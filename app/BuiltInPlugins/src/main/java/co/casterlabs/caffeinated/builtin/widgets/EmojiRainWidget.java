package co.casterlabs.caffeinated.builtin.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.commons.functional.tuples.Pair;
import lombok.SneakyThrows;

public class EmojiRainWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.emojirain_widget")
        .withIcon("face-smile")
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Emoji Rain");

    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("rain_settings", "Rain Settings")
                .addItem(WidgetSettingsItem.asNumber("life_time", "Time on Screen (Seconds)", 15, 1, 0, 128))
                .addItem(WidgetSettingsItem.asNumber("max_emojis", "Max Emojis on Screen", 1000, 1, 2, 10000))
                .addItem(WidgetSettingsItem.asNumber("size", "Emoji Size (px)", 32, 1, 0, 128))
                .addItem(WidgetSettingsItem.asRange("speed", "Fall Speed", 25, 1, 0, 100))
        );

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);
    }

    @SneakyThrows
    @Override
    public @Nullable Pair<String, String> getWidgetResource(WidgetInstanceMode mode, String resource) {
        return CaffeinatedDefaultPlugin.resolveResource("/rain.html");
    }

}

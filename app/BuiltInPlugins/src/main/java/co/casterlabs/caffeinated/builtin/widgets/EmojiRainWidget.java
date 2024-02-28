package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsNumberBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsRangeBuilder;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import lombok.NonNull;

public class EmojiRainWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.emojirain_widget")
        .withIcon("face-smile")
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Emoji Rain")
        .withShowDemo(true, 3 / 4d)
        .withRequiredFeatures(KoiIntegrationFeatures.CHAT);

    @SuppressWarnings("deprecation")
    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("rain_settings", "Rain Settings")
                .addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("life_time")
                        .withName("Time on Screen (Seconds)")
                        .withDefaultValue(15)
                        .withStep(1)
                        .withMin(0)
                        .withMax(128)
                        .build()
                )
                .addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("max_emojis")
                        .withName("Max Emojis on Screen")
                        .withDefaultValue(1000)
                        .withStep(1)
                        .withMin(2)
                        .withMax(10000)
                        .build()
                )
                .addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("size")
                        .withName("Emoji Size (px)")
                        .withDefaultValue(32)
                        .withStep(1)
                        .withMin(0)
                        .withMax(128)
                        .build()
                )
                .addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("speed")
                        .withName("Fall Speed")
                        .withDefaultValue(25)
                        .withStep(1)
                        .withMin(0)
                        .withMax(100)
                        .build()
                )
        );

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/emoji-rain";
    }

}

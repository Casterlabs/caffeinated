package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsCheckboxBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsColorBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsFontBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsNumberBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsPlatformDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsRangeBuilder;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import lombok.NonNull;

public class ChatWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.chat_widget")
        .withIcon("chat-bubble-left")
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Chat")
        .withLocaleBase("co.casterlabs.defaultwidgets.widget")
        .withShowDemo(true, 3 / 4d)
        .withRequiredFeatures(KoiIntegrationFeatures.CHAT);

    @Override
    public void onInit() {
        this.renderSettingsLayout();
    }

    @Override
    protected void onSettingsUpdate() {
        this.renderSettingsLayout();
    }

    private void renderSettingsLayout() {
        this.setSettingsLayout(this.generateSettingsLayout()); // Preserve
    }

    @SuppressWarnings("deprecation")
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        {
            WidgetSettingsSection messageStyle = new WidgetSettingsSection("message_style", "Message Style")
                .addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("margin")
                        .withName("Margin (px)")
                        .withDefaultValue(0)
                        .withMin(0)
                        .build()
                )
                .addItem(
                    new WidgetSettingsDropdownBuilder()
                        .withId("badges")
                        .withName("User Badges")
                        .withDefaultValue("Before Username")
                        .withOptionsList("Hidden", "Before Username", "After Username")
                        .build()
                )
                .addItem(
                    new WidgetSettingsCheckboxBuilder()
                        .withId("show_platform_icon")
                        .withName("Show User Platform Icon")
                        .withDefaultValue(false)
                        .build()
                );

            messageStyle.addItem(
                new WidgetSettingsDropdownBuilder()
                    .withId("username_color")
                    .withName("Display Name Color")
                    .withDefaultValue("User's Preference")
                    .withOptionsList("User's Preference", "Static Color", "Match Platform's Theme")
                    .build()
            );
            if ("Static Color".equals(this.settings().getString("message_style.username_color"))) {
                messageStyle.addItem(
                    new WidgetSettingsColorBuilder()
                        .withId("username_color.static")
                        .withName("Static Username Color")
                        .withDefaultValue("#f04f88")
                        .build()
                );
            }

            messageStyle
                .addItem(
                    new WidgetSettingsCheckboxBuilder()
                        .withId("disappearing")
                        .withName("Make Messages Disappear?")
                        .withDefaultValue(false)
                        .build()
                );
            if (this.settings().getBoolean("message_style.disappearing", false)) {
                messageStyle.addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("disappear_after")
                        .withName("Disappear After (seconds)")
                        .withDefaultValue(30)
                        .withMin(10)
                        .withMax(360)
                        .build()
                );
            }

            messageStyle.addItem(
                new WidgetSettingsDropdownBuilder()
                    .withId("message_style")
                    .withName("Message Style")
                    .withDefaultValue("Text (Bottom-up)")
                    .withOptionsList("Text (Top-down)", "Text (Bottom-up)", "Text (Sideways)")
                    .build()
            );
            if ("Text (Bottom-up)".equals(this.settings().getString("message_style.message_style")) ||
                "Text (Top-down)".equals(this.settings().getString("message_style.message_style"))) {
                messageStyle.addItem(
                    new WidgetSettingsDropdownBuilder()
                        .withId("messages_animation")
                        .withName("Message Animation")
                        .withDefaultValue("None")
                        .withOptionsList("None", "Slide-in")
                        .build()
                );

                if ("Slide-in".equals(this.settings().getString("message_style.messages_animation"))) {
                    messageStyle.addItem(
                        new WidgetSettingsDropdownBuilder()
                            .withId("slide_direction")
                            .withName("Slide-in Direction")
                            .withDefaultValue("From the left")
                            .withOptionsList("From the left", "From the right")
                            .build()
                    );
                }
            } else if ("Text (Sideways)".equals(this.settings().getString("message_style.message_style"))) {
                messageStyle.addItem(
                    new WidgetSettingsDropdownBuilder()
                        .withId("messages_animation")
                        .withName("Message Animation")
                        .withDefaultValue("None")
                        .withOptionsList("None", "Slide-in")
                        .build()
                );
            }

            layout.addSection(messageStyle);
        }

        {
            WidgetSettingsSection textStyle = new WidgetSettingsSection("text_style", "Text Style")
                .addItem(
                    new WidgetSettingsFontBuilder()
                        .withId("font")
                        .withName("Font")
                        .withDefaultValue("Poppins")
                        .build()
                )
                .addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("font_size")
                        .withName("Font Size")
                        .withDefaultValue(16)
                        .withStep(1)
                        .withMin(0)
                        .withMax(128)
                        .build()
                )
                .addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("font_weight")
                        .withName("Font Weight (Boldness)")
                        .withDefaultValue(400)
                        .withStep(100)
                        .withMin(100)
                        .withMax(1000)
                        .build()
                )
                .addItem(
                    new WidgetSettingsColorBuilder()
                        .withId("text_color")
                        .withName("Text Color")
                        .withDefaultValue("#ffffff")
                        .build()
                )
                .addItem(
                    new WidgetSettingsDropdownBuilder()
                        .withId("text_align")
                        .withName("Text Align")
                        .withDefaultValue("Left")
                        .withOptionsList("Left", "Right")
                        .build()
                )
                .addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("text_shadow")
                        .withName("Text Shadow")
                        .withDefaultValue(0)
                        .withStep(1)
                        .withMin(-1)
                        .withMax(20)
                        .build()
                )
                .addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("letter_spacing")
                        .withName("Letter Spacing")
                        .withDefaultValue(1)
                        .withStep(1)
                        .withMin(-5)
                        .withMax(5)
                        .build()
                );

            textStyle.addItem(
                new WidgetSettingsRangeBuilder()
                    .withId("outline_width")
                    .withName("Outline Size")
                    .withDefaultValue(0)
                    .withStep(.01)
                    .withMin(0)
                    .withMax(1)
                    .build()
            );
            if (this.settings().getNumber("text_style.outline_width", 0).doubleValue() > 0) {
                textStyle.addItem(
                    new WidgetSettingsColorBuilder()
                        .withId("outline_color")
                        .withName("Outline Color")
                        .withDefaultValue("#000000")
                        .build()
                );
            }

            layout.addSection(textStyle);
        }

        layout
            .addSection(
                new WidgetSettingsSection("events", "Events")
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("RICH_MESSAGE.REGULAR")
                            .withName("Show Chat Messages")
                            .withDefaultValue(true)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("CHANNEL_POINTS")
                            .withName("Show Channel Points Redemptions")
                            .withDefaultValue(true)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("RICH_MESSAGE.DONATION")
                            .withName("Show Donations Events and Messages")
                            .withDefaultValue(true)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("FOLLOW")
                            .withName("Show Follow Events")
                            .withDefaultValue(true)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("SUBSCRIPTION")
                            .withName("Show Subscription Events")
                            .withDefaultValue(true)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("RAID")
                            .withName("Show Raid Events")
                            .withDefaultValue(true)
                            .build()
                    )
            )
            .addSection(
                new WidgetSettingsSection("platform", "Platform")
                    .addItem(
                        new WidgetSettingsPlatformDropdownBuilder()
                            .withId("platforms")
                            .withName("Use from")
                            .withAllowMultiple(true)
                            .withRequiredFeatures(new KoiIntegrationFeatures[0])
                            .build()
                    )

            )
            .addButton(
                new WidgetSettingsButton(
                    "clear-chat",
                    "x-circle",
                    "Clear Chat",
                    "Clear Chat",
                    () -> {
                        this.broadcastToAll("clear");
                    }
                )
            );

        return layout;
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/chat";
    }

}

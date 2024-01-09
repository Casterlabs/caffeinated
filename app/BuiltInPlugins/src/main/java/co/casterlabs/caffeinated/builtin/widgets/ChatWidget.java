package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsCheckboxBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsColorBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsNumberBuilder;
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

        layout
            .addSection(
                new WidgetSettingsSection("text_style", "Text Style")
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asRange("font_size", "Font Size", 16, 1, 0, 128))
                    .addItem(WidgetSettingsItem.asRange("font_weight", "Font Weight (boldness)", 400, 100, 100, 1000))
                    .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                    .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right"))
                    .addItem(WidgetSettingsItem.asRange("text_shadow", "Text Shadow", -1, 1, -1, 20))
            )
            .addSection(
                new WidgetSettingsSection("events", "Events")
                    .addItem(WidgetSettingsItem.asCheckbox("RICH_MESSAGE.REGULAR", "Show Chat Messages", true))
                    .addItem(WidgetSettingsItem.asCheckbox("CHANNEL_POINTS", "Show Channel Points Redemptions", true))
                    .addItem(WidgetSettingsItem.asCheckbox("RICH_MESSAGE.DONATION", "Show Donations Events and Messages", true))
                    .addItem(WidgetSettingsItem.asCheckbox("FOLLOW", "Show Follow Events", true))
                    .addItem(WidgetSettingsItem.asCheckbox("SUBSCRIPTION", "Show Subscription Events", true))
                    .addItem(WidgetSettingsItem.asCheckbox("RAID", "Show Raid Events", true))
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

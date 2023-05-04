package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import lombok.NonNull;

public class ChatWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.chat_widget")
        .withIcon("chat-bubble-left")
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Chat Widget")
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
                .addItem(WidgetSettingsItem.asNumber("margin", "Margin (px)", 0, 1, 0, Integer.MAX_VALUE))
                .addItem(WidgetSettingsItem.asDropdown("message_direction", "Messages Direction", "Bottom-up", "Bottom-up", "Top-down"))
                .addItem(WidgetSettingsItem.asDropdown("badges", "Badges", "Before Username", "Hidden", "Before Username", "After Username"))
                .addItem(WidgetSettingsItem.asCheckbox("show_platform_icon", "Show User's Platform", false))
                .addItem(
                    WidgetSettingsItem.asDropdown(
                        "username_color", "Username color",
                        "User's Preference",
                        "User's Preference", "Static Color", "Match Platform's Theme"
                    )
                );
//                .addItem(WidgetSettingsItem.asCheckbox("show_avatars", "Show user avatars", false));

            if ("Static Color".equals(this.settings().getString("message_style.username_color"))) {
                messageStyle.addItem(WidgetSettingsItem.asColor("username_color.static", "Static Username Color", "#f04f88"));
            }

            messageStyle.addItem(WidgetSettingsItem.asCheckbox("disappearing", "Make messages disappear", false));
            if (this.settings().getBoolean("message_style.disappearing", false)) {
                messageStyle.addItem(WidgetSettingsItem.asNumber("disappear_after", "Disappear After (Seconds)", 30, 1, 10, 360));
            }

            messageStyle.addItem(WidgetSettingsItem.asDropdown("messages_animation", "Message Animation", "None", "None", "Slide-in"));
            if ("Slide-in".equals(this.settings().getString("message_style.messages_animation"))) {
                messageStyle.addItem(
                    WidgetSettingsItem.asDropdown(
                        "slide_direction", "Slide direction",
                        "From the left",
                        "From the left", "From the right"
                    )
                );
            }

//            messageStyle.addItem(WidgetSettingsItem.asDropdown("message_style", "Message Style", "Text", "Text", "Card"));
//            if ("Card".equals(this.settings().getString("message_style.message_style"))) {
//                messageStyle.addItem(WidgetSettingsItem.asColor("message_style.message_style.card_color", "Card Color", "#6e6466"));
//            }

            layout.addSection(messageStyle);
        }

        layout
            .addSection(
                new WidgetSettingsSection("text_style", "Text Style")
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                    .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                    .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right"))
            )
//            .addSection(
//                new WidgetSettingsSection("moderation", "Moderation")
//                    .addItem(WidgetSettingsItem.asCheckbox("hide_bots", "Hide Bots", true))
//                    .addItem(WidgetSettingsItem.asCheckbox("hide_naughty_language", "Hide Naughty Language", true))
//            );
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

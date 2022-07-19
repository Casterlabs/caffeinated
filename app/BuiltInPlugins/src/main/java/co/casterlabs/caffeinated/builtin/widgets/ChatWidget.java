package co.casterlabs.caffeinated.builtin.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import lombok.SneakyThrows;

public class ChatWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.chat_widget")
        .withIcon("message-square")
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Chat Widget");

    private final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("chat_style", "Style")
                .addItem(WidgetSettingsItem.asDropdown("chat_direction", "Chat Direction", "Down", "Down", "Up"))
                .addItem(WidgetSettingsItem.asDropdown("chat_animation", "Chat Animation", "None", "None", "Slide in from the left", "Slide in from the right"))
                .addItem(WidgetSettingsItem.asCheckbox("disappearing", "Make messages disappear", false))
                .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right"))
                .addItem(WidgetSettingsItem.asCheckbox("show_user_badges", "Show User Badges", true))
                .addItem(WidgetSettingsItem.asCheckbox("show_donations", "Show Donations", true))
        )
//        .addSection(
//            new WidgetSettingsSection("moderation", "Moderation")
//                .addItem(WidgetSettingsItem.asCheckbox("hide_bots", "Hide Bots", true))
//                .addItem(WidgetSettingsItem.asCheckbox("hide_naughty_language", "Hide Naughty Language", true))
//        );
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

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/chat.html");
    }

}

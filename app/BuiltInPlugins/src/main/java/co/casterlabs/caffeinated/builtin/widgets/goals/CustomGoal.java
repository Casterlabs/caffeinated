package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

public class CustomGoal extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.custom_goal")
        .withIcon("bar-chart")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Custom Goal");

    private double count;

    @Override
    public void onInit() {
        this.renderSettingsLayout();

        if (this.settings().has("goal.value")) {
            this.update(this.settings().getNumber("goal.value").doubleValue());
        }
    }

    @Override
    protected void onSettingsUpdate() {
        this.renderSettingsLayout();

        this.update(this.settings().getNumber("goal.value").doubleValue());
    }

    private void renderSettingsLayout() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();

        this.setSettingsLayout(layout, true); // Preserve
    }

    /**
     * Override as neeeded.
     */
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        String currentStyle = this.settings().has("style.style") ? this.settings().getString("style.style") : "Text";

        {
            WidgetSettingsSection barStyle = new WidgetSettingsSection("style", "Style")
                .addItem(WidgetSettingsItem.asDropdown("style", "Style", "Progress Bar (With Text)", "Progress Bar (With Text)", "Text", "Progress Bar (Without Text)"));

            if (!currentStyle.equals("Progress Bar (Without Text)")) {
                barStyle
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128));

                if (currentStyle.equals("Text")) {
                    barStyle.addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"));
                }

                barStyle.addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"));
            }

            layout.addSection(barStyle);
        }

        {
            WidgetSettingsSection barGoal = new WidgetSettingsSection("goal", "Goal");

            if (currentStyle.equals("Progress Bar (Without Text)")) {
                barGoal.addItem(WidgetSettingsItem.asColor("bar_color", "Bar Color", "#31f8ff"));
            } else {
                barGoal
                    .addItem(WidgetSettingsItem.asText("title", "Title", "", ""))
                    .addItem(WidgetSettingsItem.asNumber("target", "Target", 10, 1, 0, Integer.MAX_VALUE));

                if (currentStyle.equals("Progress Bar (With Text)")) {
                    barGoal
                        .addItem(WidgetSettingsItem.asCheckbox("add_numbers", "Add Numbers", true))
                        .addItem(WidgetSettingsItem.asColor("bar_color", "Bar Color", "#31f8ff"));
                }

            }

            barGoal.addItem(WidgetSettingsItem.asNumber("value", "Value", 1, 1, 0, Integer.MAX_VALUE));

            layout.addSection(barGoal);
        }

        return layout;
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/goal.html");
    }

    public void update(double count) {
        this.count = count;

        this.broadcastToAll("count", JsonObject.singleton("count", this.count));
    }

    @Override
    public void onNewInstance(@NonNull WidgetInstance instance) {
        try {
            instance.emit("count", JsonObject.singleton("count", this.count));
        } catch (IOException ignored) {}
    }

}

package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.io.IOException;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public class CustomGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.custom_goal")
        .withIcon("chart-bar")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Custom Goal")
        .withShowDemo(true, DEMO_ASPECT_RATIO);

    private double count;

    @Override
    public void onInit() {
        if (this.settings().has("goal.value")) {
            this.update(this.settings().getNumber("goal.value").doubleValue());
        }

        super.renderSettingsLayout();
    }

    @Override
    protected void onSettingsUpdate() {
        super.onSettingsUpdate();
        this.update(this.settings().getNumber("goal.value").doubleValue());
    }

    @Override
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

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

    @Override
    protected boolean enableValueSetting() {
        return true;
    }

}

package co.casterlabs.caffeinated.builtin.widgets.goals;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;

public class CustomGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.custom_goal")
        .withIcon("chart-bar")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Custom Goal")
        .withShowDemo(true, DEMO_ASPECT_RATIO);

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
        this.settings().set("goal.value", count);
        super.update(count);
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

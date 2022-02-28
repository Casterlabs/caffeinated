package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.builtin.widgets.goals.generic.GenericGoal;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

public class CustomGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.custom_goal")
        .withIcon("bar-chart")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Custom Goal");

    private double count;

    @Override
    public void onInit() {
        if (this.settings().has("goal.value")) {
            this.update(this.settings().getNumber("goal.value").doubleValue());
        }
    }

    @Override
    protected void onSettingsUpdate() {
        this.update(this.settings().getNumber("goal.value").doubleValue());
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/goal.html");
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

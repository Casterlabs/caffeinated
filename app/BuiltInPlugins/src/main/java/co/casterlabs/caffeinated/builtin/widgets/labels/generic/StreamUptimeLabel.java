package co.casterlabs.caffeinated.builtin.widgets.labels.generic;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import lombok.SneakyThrows;

public class StreamUptimeLabel extends GenericLabel {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.stream_uptime_label")
        .withIcon("clock")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Stream Uptime Label");

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    protected boolean hasHighlight() {
        return false;
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/streamuptime.html");
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

}

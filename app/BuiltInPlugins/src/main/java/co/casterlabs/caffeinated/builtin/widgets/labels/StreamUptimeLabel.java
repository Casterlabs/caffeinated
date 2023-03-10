package co.casterlabs.caffeinated.builtin.widgets.labels;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import lombok.NonNull;

public class StreamUptimeLabel extends GenericLabel {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.stream_uptime_label")
        .withIcon("clock")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Stream Uptime Label")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.STREAM_STATUS);

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    protected boolean hasHighlight() {
        return false;
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/stream-uptime";
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

}

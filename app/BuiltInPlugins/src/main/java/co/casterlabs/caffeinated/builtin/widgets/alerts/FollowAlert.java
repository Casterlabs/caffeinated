package co.casterlabs.caffeinated.builtin.widgets.alerts;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.FollowEvent;

public class FollowAlert extends GenericAlert implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.follow_alert")
        .withIcon("user")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Follow Alert")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.FOLLOWER_ALERT)
        .withTestEvents(KoiEventType.FOLLOW);

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onFollow(FollowEvent e) {
        if (!this.getSelectedPlatforms().contains(e.streamer.platform)) return;

        this.queueAlert(e, null, null);
    }

    @Override
    protected String defaultFormat() {
        return "${escapeHtml(event.follower.displayname)} just followed!";
    }

    @Override
    protected boolean hasCustomImageImplementation() {
        return false;
    }

    @Override
    protected boolean hasTTS() {
        return false;
    }

    @Override
    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[] {
                KoiIntegrationFeatures.FOLLOWER_ALERT
        };
    }

}

package co.casterlabs.caffeinated.builtin.widgets.alerts;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.RaidEvent;

public class RaidAlert extends GenericAlert implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.raid_alert")
        .withIcon("user-group")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Raid Alert")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.RAID_ALERT)
        .withTestEvents(KoiEventType.RAID);

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onRaid(RaidEvent e) {
        if (!this.getSelectedPlatforms().contains(e.streamer.platform)) return;

        this.queueAlert(e, null, null);
    }

    @Override
    protected String defaultFormat() {
        return "${escapeHtml(event.host.displayname)} is raiding with ${event.viewers} viewers!";
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
                KoiIntegrationFeatures.RAID_ALERT
        };
    }

}

package co.casterlabs.caffeinated.builtin.widgets.alerts;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.RaidEvent;

public class RaidAlert extends GenericAlert implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.raid_alert")
        .withIcon("user-group")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Raid Alert")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.RAID_ALERT);

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onRaid(RaidEvent e) {
        // Generate the title html.
        String title = String.format("<span class='highlight'>%s</span>", e.getHost().getDisplayname());
        String title2 = String.format("<span class='highlight'>%s</span>", e.getViewers());

        this.queueAlert(title, title2, null, null, null);
    }

    @Override
    protected String defaultPrefix() {
        return "";
    }

    @Override
    protected String defaultInfix() {
        return "is raiding with";
    }

    @Override
    protected String defaultSuffix() {
        return "viewers!";
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
    protected boolean hasInfix() {
        return true;
    }

}

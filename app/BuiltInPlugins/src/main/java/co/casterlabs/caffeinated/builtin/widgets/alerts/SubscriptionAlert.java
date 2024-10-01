package co.casterlabs.caffeinated.builtin.widgets.alerts;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;

public class SubscriptionAlert extends GenericAlert implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.subscription_alert")
        .withIcon("user-plus")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Subscription Alert")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.SUBSCRIPTION_ALERT)
        .withTestEvents(KoiEventType.SUBSCRIPTION);

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onSubscriber(SubscriptionEvent e) {
        if (!this.getSelectedPlatforms().contains(e.streamer.platform)) return;

        this.queueAlert(e, null, null);
    }

    @Override
    protected String defaultFormat() {
        // TODO
//        if (e.getGiftRecipient() != null) {
//            sub = e.getGiftRecipient();
//        } else {
//            sub = e.getSubscriber();
//        }
        return "${(event.gift_recipient || event.subscriber).displayname} just subscribed!";
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
                KoiIntegrationFeatures.SUBSCRIPTION_ALERT
        };
    }

}

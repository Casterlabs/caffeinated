package co.casterlabs.caffeinated.builtin.widgets.alerts;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.user.User;

public class SubscriptionAlert extends GenericAlert implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.subscription_alert")
        .withIcon("user-plus")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Subscription Alert")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.SUBSCRIPTION_ALERT);

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onSubscriber(SubscriptionEvent e) {
        User sub = null;

        if (e.getGiftRecipient() != null) {
            sub = e.getGiftRecipient();
        } else {
            sub = e.getSubscriber();
        }

        // Generate the title html.
        String title = String.format("<span class='highlight'>%s</span>", sub.getDisplayname());

        this.queueAlert(title, null, null, null);
    }

    @Override
    protected String defaultPrefix() {
        return "";
    }

    @Override
    protected String defaultSuffix() {
        return "just subscribed!";
    }

    @Override
    protected boolean hasCustomImageImplementation() {
        return false;
    }

    @Override
    protected boolean hasTTS() {
        return false;
    }

}

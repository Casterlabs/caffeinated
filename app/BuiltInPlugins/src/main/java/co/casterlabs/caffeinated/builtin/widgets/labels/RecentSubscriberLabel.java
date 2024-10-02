package co.casterlabs.caffeinated.builtin.widgets.labels;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public class RecentSubscriberLabel extends GenericLabel {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.recent_subscriber_label")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Recent Subscriber Label")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.SUBSCRIPTION_ALERT)
        .withTestEvents(KoiEventType.SUBSCRIPTION);

    private User recentSubscriber;
    private String currHtml = "";

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);

        // If this fails then we don't care.
        try {
            JsonElement recentSubscriber = this.settings().get("recent_subscriber");

            this.recentSubscriber = Rson.DEFAULT.fromJson(recentSubscriber, User.class);
        } catch (Exception ignored) {}

        this.updateText();
    }

    private void save() {
        this.settings().set("recent_subscriber", Rson.DEFAULT.toJson(this.recentSubscriber));
    }

    @Override
    protected List<WidgetSettingsButton> getButtons() {
        return Arrays.asList(
            new WidgetSettingsButton("reset")
                .withIcon("no-symbol")
                .withIconTitle("Reset Counter")
                .withText("Reset Counter")
                .withOnClick(() -> {
                    this.recentSubscriber = null;

                    this.save();
                    this.updateText();
                })
        );
    }

    @Override
    protected void onSettingsUpdate() {
        this.updateText();
    }

    @KoiEventHandler
    public void onSubscription(@Nullable SubscriptionEvent event) {
        if (event.giftRecipients != null) {
            this.recentSubscriber = event.giftRecipients.get(0);
        } else {
            this.recentSubscriber = event.subscriber;
        }

        this.save();
        this.updateText();
    }

    private void updateText() {
        if (this.recentSubscriber == null) {
            this.currHtml = "";
        } else {
            String html = this.recentSubscriber.displayname;

            String prefix = WebUtil.escapeHtml(this.settings().getString("text.prefix")).replace(" ", "&nbsp;");
            String suffix = WebUtil.escapeHtml(this.settings().getString("text.suffix")).replace(" ", "&nbsp;");

            if (!prefix.isEmpty()) {
                html = prefix + ' ' + html;
            }

            if (!suffix.isEmpty()) {
                html = html + ' ' + suffix;
            }

            this.currHtml = html;
        }

        this.broadcastToAll("html", JsonObject.singleton("html", this.currHtml));
    }

    @Override
    public void onNewInstance(@NonNull WidgetInstance instance) {
        try {
            instance.emit("html", JsonObject.singleton("html", this.currHtml));
        } catch (IOException ignored) {}
    }

    @Override
    protected boolean hasHighlight() {
        return false;
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

}

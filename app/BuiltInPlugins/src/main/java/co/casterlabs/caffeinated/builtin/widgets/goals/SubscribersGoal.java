package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;

public class SubscribersGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.subscribers_goal")
        .withIcon("user-plus")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Subscribers Goal")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.SUBSCRIBER_COUNT, KoiIntegrationFeatures.SUBSCRIPTION_ALERT);

    private Map<String, Integer> offsets = new HashMap<>();

    @Override
    public void onInit() {
        this.addKoiListener(this);
        this.recalculate();
    }

    @Override
    protected void onSettingsUpdate() {
        super.onSettingsUpdate();
        this.recalculate();
    }

    @KoiEventHandler
    public void onSubscribe(SubscriptionEvent e) {
        if (this.offsets.containsKey(e.getStreamer().getUPID())) {
            int newOffset = this.offsets.get(e.getStreamer().getUPID()) + 1;
            this.offsets.put(e.getStreamer().getUPID(), newOffset);
        } else {
            this.offsets.put(e.getStreamer().getUPID(), 1);
        }
        this.recalculate();
    }

    @KoiEventHandler
    public void onUserUpdate(UserUpdateEvent e) {
        this.offsets.remove(e.getStreamer().getUPID()); // Clear the offset value. We just got a fresh count!
        this.recalculate();
    }

    private void recalculate() {
        long subCount = 0;

        for (UserPlatform platform : this.getSelectedPlatforms()) {
            UserUpdateEvent state = Caffeinated.getInstance().getKoi().getUserStates().get(platform);
            if (state == null) continue;

            long stateSubs = state.getStreamer().getSubCount();
            if (stateSubs != -1) {
                subCount += stateSubs;

                if (this.offsets.containsKey(state.getStreamer().getUPID())) {
                    subCount += this.offsets.get(state.getStreamer().getUPID());
                }
            }
        }

        this.update(subCount);
    }

    @Override
    protected boolean enablePlatformOption() {
        return true;
    }

    @Override
    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[] {
                KoiIntegrationFeatures.SUBSCRIBER_COUNT,
                KoiIntegrationFeatures.SUBSCRIPTION_ALERT
        };
    }

}

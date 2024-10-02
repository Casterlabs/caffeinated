package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;

public class DailyFollowersGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.daily_followers_goal")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Daily Followers Goal")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.FOLLOWER_ALERT, KoiIntegrationFeatures.STREAM_STATUS);

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
    public void onFollow(FollowEvent e) {
        if (this.offsets.containsKey(e.streamer.UPID)) {
            int newOffset = this.offsets.get(e.streamer.UPID) + 1;
            this.offsets.put(e.streamer.UPID, newOffset);
        }
        this.recalculate();
    }

    @KoiEventHandler
    public void onStreamStatus(StreamStatusEvent e) {
        if (e.live) {
            this.offsets.putIfAbsent(e.streamer.UPID, 0);
        } else {
            this.offsets.remove(e.streamer.UPID); // Clear the offset value. No longer live.
        }
        this.recalculate();
    }

    private void recalculate() {
        long count = 0;
        for (Integer c : this.offsets.values()) {
            count += c;
        }
        this.update(count);
    }

    @Override
    protected boolean enablePlatformOption() {
        return true;
    }

    @Override
    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[] {
                KoiIntegrationFeatures.FOLLOWER_ALERT,
                KoiIntegrationFeatures.STREAM_STATUS
        };
    }

}

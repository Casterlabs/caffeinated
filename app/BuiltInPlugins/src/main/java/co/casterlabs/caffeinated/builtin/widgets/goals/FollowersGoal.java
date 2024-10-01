package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;

public class FollowersGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.followers_goal")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Followers Goal")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.FOLLOWER_COUNT, KoiIntegrationFeatures.FOLLOWER_ALERT);

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
        } else {
            this.offsets.put(e.streamer.UPID, 1);
        }
        this.recalculate();
    }

    @KoiEventHandler
    public void onUserUpdate(UserUpdateEvent e) {
        this.offsets.remove(e.streamer.UPID); // Clear the offset value. We just got a fresh count!
        this.recalculate();
    }

    private void recalculate() {
        long followersCount = 0;

        for (UserPlatform platform : this.getSelectedPlatforms()) {
            UserUpdateEvent state = Caffeinated.getInstance().getKoi().getUserStates().get(platform);
            if (state == null) continue;

            long stateFollows = state.streamer.followersCount;
            if (stateFollows != -1) {
                followersCount += stateFollows;

                if (this.offsets.containsKey(state.streamer.UPID)) {
                    followersCount += this.offsets.get(state.streamer.UPID);
                }
            }
        }

        this.update(followersCount);
    }

    @Override
    protected boolean enablePlatformOption() {
        return true;
    }

    @Override
    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[] {
                KoiIntegrationFeatures.FOLLOWER_COUNT,
                KoiIntegrationFeatures.FOLLOWER_ALERT
        };
    }

}

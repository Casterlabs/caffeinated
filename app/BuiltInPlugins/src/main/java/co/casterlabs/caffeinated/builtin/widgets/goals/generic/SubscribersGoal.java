package co.casterlabs.caffeinated.builtin.widgets.goals.generic;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;

public class SubscribersGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.subscribers_goal")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Subscribers Goal");

    @Override
    public void onInit() {
        this.addKoiListener(this);
        this.onUserUpdate(null);
    }

    @Override
    protected void onSettingsUpdate() {
        super.onSettingsUpdate();
        this.onUserUpdate(null);
    }

    @KoiEventHandler
    public void onUserUpdate(@Nullable UserUpdateEvent _ignored) {
        UserPlatform platform = this.getSelectedPlatform();

        if (platform != null) {
            this.update(Caffeinated.getInstance().getKoi().getUserStates().get(platform).getStreamer().getSubCount());
        }
    }

    @Override
    protected boolean enablePlatformOption() {
        return true;
    }

}

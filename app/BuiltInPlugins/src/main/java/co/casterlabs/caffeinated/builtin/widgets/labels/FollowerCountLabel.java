package co.casterlabs.caffeinated.builtin.widgets.labels;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public class FollowerCountLabel extends GenericLabel {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.follower_count_label")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Follower Count Label")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.FOLLOWER_COUNT);

    private String currHtml = "";

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);
        this.onUserUpdate(null);
    }

    @Override
    protected void onSettingsUpdate() {
        this.onUserUpdate(null);
    }

    @KoiEventHandler
    public void onUserUpdate(@Nullable UserUpdateEvent _ignored) {
        long followersCount = 0;

        for (UserPlatform platform : this.getSelectedPlatforms()) {
            UserUpdateEvent state = Caffeinated.getInstance().getKoi().getUserStates().get(platform);
            if (state == null) continue;

            long stateFollows = state.streamer.followersCount;
            if (stateFollows != -1) {
                followersCount += stateFollows;
            }
        }

        this.updateText(followersCount);
    }

    private void updateText(long followerCount) {
        String html = String.valueOf(followerCount);

        String prefix = WebUtil.escapeHtml(this.settings().getString("text.prefix")).replace(" ", "&nbsp;");
        String suffix = WebUtil.escapeHtml(this.settings().getString("text.suffix")).replace(" ", "&nbsp;");

        if (!prefix.isEmpty()) {
            html = prefix + ' ' + html;
        }

        if (!suffix.isEmpty()) {
            html = html + ' ' + suffix;
        }

        this.currHtml = html;

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
        return true;
    }

    @Override
    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[] {
                KoiIntegrationFeatures.FOLLOWER_COUNT
        };
    }

}

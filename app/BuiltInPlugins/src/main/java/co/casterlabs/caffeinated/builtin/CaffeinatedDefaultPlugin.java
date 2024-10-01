package co.casterlabs.caffeinated.builtin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.locale.BuiltInLocale;
import co.casterlabs.caffeinated.builtin.widgets.CamWidget;
import co.casterlabs.caffeinated.builtin.widgets.ChatWidget;
import co.casterlabs.caffeinated.builtin.widgets.EmojiRainWidget;
import co.casterlabs.caffeinated.builtin.widgets.NowPlayingWidget;
import co.casterlabs.caffeinated.builtin.widgets.alerts.DonationAlert;
import co.casterlabs.caffeinated.builtin.widgets.alerts.FollowAlert;
import co.casterlabs.caffeinated.builtin.widgets.alerts.RaidAlert;
import co.casterlabs.caffeinated.builtin.widgets.alerts.SubscriptionAlert;
import co.casterlabs.caffeinated.builtin.widgets.goals.CustomGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.DailyFollowersGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.DailySubscribersGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.DonationGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.FollowersGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.SubscribersGoal;
import co.casterlabs.caffeinated.builtin.widgets.labels.DonationTotalLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.FollowerCountLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.RecentDonationLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.RecentFollowerLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.RecentSubscriberLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.StreamUptimeLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.SubscriberCountLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.TopDonationLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.ViewersCountLabel;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.localization.LocaleProvider;
import lombok.NonNull;

public class CaffeinatedDefaultPlugin extends CaffeinatedPlugin {

    @SuppressWarnings("unused")
    @Override
    public void onInit() {
        CaffeinatedYoutubePlugin.init(this);

        // I spend way too long on this shit.
        this.getLogger().info(" _________________");
        this.getLogger().info("|       Hi!       |");
        this.getLogger().info("|   My name is:   |");
        this.getLogger().info("|‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾|");
        this.getLogger().info("|   Casterlabs    |");
        this.getLogger().info("|                 |");
        this.getLogger().info("|                 |");
        this.getLogger().info(" ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾ ");

        // Interaction
        Caffeinated.getInstance().getPlugins().registerWidget(this, ChatWidget.DETAILS, ChatWidget.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, EmojiRainWidget.DETAILS, EmojiRainWidget.class);

        // Labels
        Caffeinated.getInstance().getPlugins().registerWidget(this, FollowerCountLabel.DETAILS, FollowerCountLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, SubscriberCountLabel.DETAILS, SubscriberCountLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, ViewersCountLabel.DETAILS, ViewersCountLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, StreamUptimeLabel.DETAILS, StreamUptimeLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, RecentSubscriberLabel.DETAILS, RecentSubscriberLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, RecentFollowerLabel.DETAILS, RecentFollowerLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, RecentDonationLabel.DETAILS, RecentDonationLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, TopDonationLabel.DETAILS, TopDonationLabel.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, DonationTotalLabel.DETAILS, DonationTotalLabel.class);

        // Other
        Caffeinated.getInstance().getPlugins().registerWidget(this, NowPlayingWidget.DETAILS, NowPlayingWidget.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, CamWidget.DETAILS, CamWidget.class);
//        Caffeinated.getInstance().getPlugins().registerWidget(this, KeyboardWidget.DETAILS, KeyboardWidget.class);
        // TODO Video Share

        // Goals
        Caffeinated.getInstance().getPlugins().registerWidget(this, FollowersGoal.DETAILS, FollowersGoal.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, SubscribersGoal.DETAILS, SubscribersGoal.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, CustomGoal.DETAILS, CustomGoal.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, DonationGoal.DETAILS, DonationGoal.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, DailyFollowersGoal.DETAILS, DailyFollowersGoal.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, DailySubscribersGoal.DETAILS, DailySubscribersGoal.class);

        // Alerts
        Caffeinated.getInstance().getPlugins().registerWidget(this, DonationAlert.DETAILS, DonationAlert.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, FollowAlert.DETAILS, FollowAlert.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, SubscriptionAlert.DETAILS, SubscriptionAlert.class);
        Caffeinated.getInstance().getPlugins().registerWidget(this, RaidAlert.DETAILS, RaidAlert.class);

        // Services
        this.registerService("goals", new GoalsService(this));
    }

    @Override
    public void onClose() {

    }

    @Override
    public @NonNull String getName() {
        return "Casterlabs Default Widgets";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.defaultwidgets";
    }

    @Override
    public @Nullable Map<String, LocaleProvider> getLang() {
        return BuiltInLocale.providers;
    }

    @Override
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        // Append `index.html` to the end when required.
        if (!resource.contains(".")) {
            if (resource.endsWith("/")) {
                resource += "index.html";
            } else {
                resource += ".html";
            }
        }

        String mimeType = "application/octet-stream";

        String[] split = resource.split("\\.");
        if (split.length > 1) {
            mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
        }

        resource = "co/casterlabs/caffeinated/builtin/html" + resource;
        this.getLogger().debug("Loading resource: %s", resource);

        try (InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream(resource)) {
            return new Pair<>(
                StreamUtil.toString(in, StandardCharsets.UTF_8),
                mimeType
            );
        } catch (Throwable e) {
            this.getLogger().debug("An error occurred whilst loading resource %s:\n%s", resource, e);
            return new Pair<>("", "text/plain");
        }
    }

}

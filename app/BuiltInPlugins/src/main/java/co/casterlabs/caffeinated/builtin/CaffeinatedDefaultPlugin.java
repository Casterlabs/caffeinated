package co.casterlabs.caffeinated.builtin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.widgets.CamWidget;
import co.casterlabs.caffeinated.builtin.widgets.ChatWidget;
import co.casterlabs.caffeinated.builtin.widgets.EmojiRainWidget;
import co.casterlabs.caffeinated.builtin.widgets.NowPlayingWidget;
import co.casterlabs.caffeinated.builtin.widgets.alerts.generic.DonationAlert;
import co.casterlabs.caffeinated.builtin.widgets.alerts.generic.FollowAlert;
import co.casterlabs.caffeinated.builtin.widgets.alerts.generic.RaidAlert;
import co.casterlabs.caffeinated.builtin.widgets.alerts.generic.SubscriptionAlert;
import co.casterlabs.caffeinated.builtin.widgets.goals.CustomGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.DonationGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.generic.FollowersGoal;
import co.casterlabs.caffeinated.builtin.widgets.goals.generic.SubscribersGoal;
import co.casterlabs.caffeinated.builtin.widgets.labels.DonationTotalLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.RecentDonationLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.StreamUptimeLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.TopDonationLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.generic.FollowerCountLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.generic.RecentFollowerLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.generic.RecentSubscriberLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.generic.SubscriberCountLabel;
import co.casterlabs.caffeinated.builtin.widgets.labels.generic.ViewersCountLabel;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.io.IOUtil;
import lombok.NonNull;
import okhttp3.Request;

public class CaffeinatedDefaultPlugin extends CaffeinatedPlugin {

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
        this.getPlugins().registerWidget(this, ChatWidget.DETAILS, ChatWidget.class);
        this.getPlugins().registerWidget(this, EmojiRainWidget.DETAILS, EmojiRainWidget.class);

        // Labels
        this.getPlugins().registerWidget(this, FollowerCountLabel.DETAILS, FollowerCountLabel.class);
        this.getPlugins().registerWidget(this, SubscriberCountLabel.DETAILS, SubscriberCountLabel.class);
        this.getPlugins().registerWidget(this, ViewersCountLabel.DETAILS, ViewersCountLabel.class);
        this.getPlugins().registerWidget(this, StreamUptimeLabel.DETAILS, StreamUptimeLabel.class);
        this.getPlugins().registerWidget(this, RecentSubscriberLabel.DETAILS, RecentSubscriberLabel.class);
        this.getPlugins().registerWidget(this, RecentFollowerLabel.DETAILS, RecentFollowerLabel.class);
        this.getPlugins().registerWidget(this, RecentDonationLabel.DETAILS, RecentDonationLabel.class);
        this.getPlugins().registerWidget(this, TopDonationLabel.DETAILS, TopDonationLabel.class);
        this.getPlugins().registerWidget(this, DonationTotalLabel.DETAILS, DonationTotalLabel.class);

        // Other
        this.getPlugins().registerWidget(this, NowPlayingWidget.DETAILS, NowPlayingWidget.class);
        this.getPlugins().registerWidget(this, CamWidget.DETAILS, CamWidget.class);
//        this.getPlugins().registerWidget(this, KeyboardWidget.DETAILS, KeyboardWidget.class);
        // TODO Video Share

        // Goals
        this.getPlugins().registerWidget(this, FollowersGoal.DETAILS, FollowersGoal.class);
        this.getPlugins().registerWidget(this, SubscribersGoal.DETAILS, SubscribersGoal.class);
        this.getPlugins().registerWidget(this, CustomGoal.DETAILS, CustomGoal.class);
        this.getPlugins().registerWidget(this, DonationGoal.DETAILS, DonationGoal.class);

        // Alerts
        this.getPlugins().registerWidget(this, DonationAlert.DETAILS, DonationAlert.class);
        this.getPlugins().registerWidget(this, FollowAlert.DETAILS, FollowAlert.class);
        this.getPlugins().registerWidget(this, SubscriptionAlert.DETAILS, SubscriptionAlert.class);
        this.getPlugins().registerWidget(this, RaidAlert.DETAILS, RaidAlert.class);

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
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        if (CaffeinatedPlugin.isDevEnvironment()) {
            String url = "http://localhost:3002/$caffeinated-sdk-root$" + resource;
            Pair<byte[], String> result = WebUtil.sendHttpRequestBytesWithMime(new Request.Builder().url(url));

            String content = new String(result.a(), StandardCharsets.UTF_8);
            String mimeType = result.b();

            return new Pair<>(content, mimeType);
        } else {
            // Append `index.html` to the end when required.
            if (!resource.contains(".")) {
                if (resource.endsWith("/")) {
                    resource += "index.html";
                } else {
                    resource += "/index.html";
                }
            }

            String mimeType = "application/octet-stream";

            String[] split = resource.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            try (InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream("widgets" + resource)) {
                return new Pair<>(
                    IOUtil.readInputStreamString(in, StandardCharsets.UTF_8),
                    mimeType
                );
            }
        }
    }

}

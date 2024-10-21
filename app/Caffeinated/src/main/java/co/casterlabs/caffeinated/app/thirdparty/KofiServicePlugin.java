package co.casterlabs.caffeinated.app.thirdparty;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.ui.UIDocksPlugin;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Listener;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.emoji.generator.WebUtil;
import co.casterlabs.koi.api.types.MessageId;
import co.casterlabs.koi.api.types.RoomId;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionLevel;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionType;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.events.rich.Donation.DonationType;
import co.casterlabs.koi.api.types.events.rich.fragments.ChatFragment;
import co.casterlabs.koi.api.types.events.rich.fragments.TextFragment;
import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
public class KofiServicePlugin extends CaffeinatedPlugin implements KinokoV1Listener {
    private static final SimpleProfile KOFI_STREAMER = SimpleProfile.builder()
        .bothIds("kofi")
        .platform(UserPlatform.CUSTOM_INTEGRATION)
        .build();

    private KinokoV1Connection connection = new KinokoV1Connection(this);

    @SneakyThrows
    @Override
    public void onMessage(String raw) {
        if (raw.startsWith("data=")) {
            raw = raw.substring("data=".length());
            raw = WebUtil.decodeURIComponent(raw);
        }

        JsonObject json = Rson.DEFAULT.fromJson(raw, JsonObject.class);

        if (json.containsKey("is_public") && !json.getBoolean("is_public")) {
            return; // Don't show, per Kofi requirements.
        }

        this.getLogger().debug("Ko-fi event: %s", json);

        User user = User.builder(
            SimpleProfile.builder()
                .bothIds(json.getString("from_name"))
                .platform(UserPlatform.CUSTOM_INTEGRATION)
                .build()
        )
            .username(json.getString("from_name"))
            .displayname(json.getString("from_name"))
            .color("#00aff1")
            .link(json.getString("url"))
            .imageLink(getRandomAvatar())
            .build();

        switch (json.getString("type")) {
            case "Donation": {
                ChatFragment message = TextFragment.of(json.getString("message"));
                Donation donation = Donation.of(
                    DonationType.OTHER, "Donation",
                    json.getString("currency"), Double.parseDouble(json.getString("amount")), 1,
                    user.imageLink
                );

                RichMessageEvent rich = RichMessageEvent.builder(
                    MessageId.random(KOFI_STREAMER, user.toSimpleProfile()),
                    RoomId.of(KOFI_STREAMER, json.getString("url"))
                )
                    .appendDonation(donation)
                    .appendFragment(message)
                    .build();

                CaffeinatedApp.getInstance().getKoi().broadcastEvent(rich);
                break;
            }

            case "Subscription": {
                CaffeinatedApp.getInstance().getKoi().broadcastEvent(
                    SubscriptionEvent.builder()
                        .timestamp(Instant.now())
                        .streamer(KOFI_STREAMER)
                        .subscriber(user)
                        .subType(json.getBoolean("is_first_subscription_payment") ? SubscriptionType.SUB : SubscriptionType.RESUB)
                        .subLevel(SubscriptionLevel.UNKNOWN)
                        .build()
                );
                break;
            }

//            case "Shop Order": {
//                CaffeinatedApp.getInstance().notify(
//                    String.format("%s just made an order on your Ko-fi shop!", json.getString("from_name")),
//                    Collections.emptyMap(),
//                    NotificationType.INFO
//                );
//                break;
//            }
        }
    }

    @Override
    public void onOpen() {
        this.connection.getLogger().setCurrentLevel(LogLevel.INFO);
    }

    @SneakyThrows
    @Override
    public void onClose(boolean remote) {
        if (remote) {
            this.connection.connect(
                getChannel(),
                true,
                false
            );
        }
    }

    @Override
    public void onOrphaned() {}

    @Override
    public void onAdopted() {}

    @Override
    public void onInit() {
        Caffeinated.getInstance().getPlugins().registerWidget(this, KofiUI.DETAILS, KofiUI.class);
        this.onClose(true);
    }

    @Override
    public void onClose() {}

    @Override
    public @NonNull String getName() {
        return "Ko-fi Integration";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.thirdparty.kofi";
    }

    public static class KofiUI extends Widget {
        public static final WidgetDetails DETAILS = new WidgetDetails()
            .withNamespace("co.casterlabs.thirdparty.kofi.settings")
            .withType(WidgetType.SETTINGS_APPLET)
            .withFriendlyName("Ko-fi Integration");

        @Override
        public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
            return "/thirdparty/kofi/settings";
        }

        @Override
        public void onNewInstance(@NonNull WidgetInstance instance) {
            instance.on("copyUrl", () -> {
                CaffeinatedApp.getInstance().copyText(
                    "https://api.casterlabs.co/v1/kinoko?channel=" + WebUtil.encodeURIComponent(getChannel()),
                    "Copied webhook URL to clipboard."
                );
            });
        }

    }

    @Override
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        return UIDocksPlugin.resolveUIFile(resource);
    }

    private static String getRandomAvatar() {
        return String.format("https://ko-fi.com/img/anon%d.png?v=1", ThreadLocalRandom.current().nextInt(0, 19));
    }

    private static String getChannel() {
        return String.format(
            "caffeinated_api:%s:kofi",
            CaffeinatedApp.getInstance().getAppPreferences().get().getDeveloperApiKey()
        );
    }

}

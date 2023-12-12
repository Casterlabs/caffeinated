package co.casterlabs.caffeinated.app.thirdparty;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
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
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionLevel;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionType;
import co.casterlabs.koi.api.types.events.rich.ChatFragment;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.events.rich.Donation.DonationType;
import co.casterlabs.koi.api.types.events.rich.TextFragment;
import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KofiServicePlugin extends CaffeinatedPlugin implements KinokoV1Listener {
    private static final SimpleProfile KOFI_STREAMER = new SimpleProfile("kofi", "kofi", UserPlatform.CUSTOM_INTEGRATION);

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

        switch (json.getString("type")) {
            case "Donation": {
                User sender = new User(
                    json.getString("from_name"), json.getString("from_name"), UserPlatform.CUSTOM_INTEGRATION,
                    Collections.emptyList(), Collections.emptyList(),
                    "#00aff1", json.getString("from_name"), json.getString("from_name"), "", json.getString("url"), getRandomAvatar(),
                    0, 0
                );

                ChatFragment message = new TextFragment(json.getString("message"));
                Donation donation = new Donation(
                    DonationType.OTHER, "Donation",
                    json.getString("currency"), Double.parseDouble(json.getString("amount")), 1,
                    sender.getImageLink()
                );

                RichMessageEvent rich = new RichMessageEvent(
                    KOFI_STREAMER, sender, Arrays.asList(message),
                    Arrays.asList(donation), Collections.emptyList(),
                    json.getString("message_id"), null
                );

                CaffeinatedApp.getInstance().getKoi().broadcastEvent(rich);
                CaffeinatedApp.getInstance().getKoi().broadcastEvent(rich.toLegacyEvent());
                break;
            }

            case "Subscription": {
                User subscriber = new User(
                    json.getString("from_name"), json.getString("from_name"), UserPlatform.CUSTOM_INTEGRATION,
                    Collections.emptyList(), Collections.emptyList(),
                    "#00aff1", json.getString("from_name"), json.getString("from_name"), "", json.getString("url"), getRandomAvatar(),
                    0, 0
                );

                CaffeinatedApp.getInstance().getKoi().broadcastEvent(
                    new SubscriptionEvent(
                        KOFI_STREAMER, subscriber, null,
                        1, 1,
                        json.getBoolean("is_first_subscription_payment") ? SubscriptionType.SUB : SubscriptionType.RESUB,
                        SubscriptionLevel.UNKNOWN
                    )
                );
                break;
            }

            case "Shop Order": {
                CaffeinatedApp.getInstance().notify(
                    String.format("%s just made an order on your Ko-fi shop!", json.getString("from_name")),
                    Collections.emptyMap(),
                    NotificationType.INFO
                );
                break;
            }
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

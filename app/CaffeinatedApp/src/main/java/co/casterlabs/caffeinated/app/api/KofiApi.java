package co.casterlabs.caffeinated.app.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Listener;
import co.casterlabs.emoji.generator.WebUtil;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
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
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KofiApi extends JavascriptObject implements KinokoV1Listener {
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
                    json.getString("currency"), json.getNumber("amount").doubleValue(), 1,
                    sender.getImageLink()
                );

                RichMessageEvent rich = new RichMessageEvent(
                    KOFI_STREAMER, sender, Arrays.asList(message),
                    Arrays.asList(donation), Collections.emptyList(),
                    json.getString("message_id"), null
                );

                CaffeinatedApp.getInstance().getKoi().onEvent(rich);
                CaffeinatedApp.getInstance().getKoi().onEvent(rich.toLegacyEvent());
                break;
            }

            case "Subscription": {
                User subscriber = new User(
                    json.getString("from_name"), json.getString("from_name"), UserPlatform.CUSTOM_INTEGRATION,
                    Collections.emptyList(), Collections.emptyList(),
                    "#00aff1", json.getString("from_name"), json.getString("from_name"), "", json.getString("url"), getRandomAvatar(),
                    0, 0
                );

                CaffeinatedApp.getInstance().getKoi().onEvent(
                    new SubscriptionEvent(
                        KOFI_STREAMER, subscriber, null,
                        1,
                        json.getBoolean("is_first_subscription_payment") ? SubscriptionType.SUB : SubscriptionType.RESUB,
                        SubscriptionLevel.UNKNOWN
                    )
                );
                break;
            }

            case "Shop Order": {
                CaffeinatedApp.getInstance().notify(
                    String.format("%s just made an order on your Ko-fi shop!", json.getString("from_name")),
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
                this.getChannel(),
                true,
                false
            );
        }
    }

    @Override
    public void onOrphaned() {}

    @Override
    public void onAdopted() {}

    public String getChannel() {
        return String.format(
            "caffeinated_api:%s:kofi",
            CaffeinatedApp.getInstance().getAppPreferences().get().getDeveloperApiKey()
        );
    }

    @JavascriptFunction
    public String getUrl() {
        return "https://api.casterlabs.co/v1/kinoko?channel=" + WebUtil.encodeURIComponent(this.getChannel());
    }

    private static String getRandomAvatar() {
        return String.format("https://ko-fi.com/img/anon%d.png?v=1", ThreadLocalRandom.current().nextInt(0, 19));
    }

}

package co.casterlabs.koi.api;

import java.time.Instant;
import java.util.Collections;

import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionLevel;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionType;
import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@SuppressWarnings("deprecation")
public class TestEvents {

    private static User testUser = new User(
        "CASTERLABS;CASTERLABS_SYSTEM",
        "CASTERLABS",
        "CASTERLABS",
        UserPlatform.CASTERLABS_SYSTEM,
        Collections.emptyList(), // Roles
        Collections.emptyList(), // Badges
        "#dd0000", // Color
        "casterlabs", // Username
        "Casterlabs", // Displayname
        "I <3 widgets!", // Bio
        "https://casterlabs.co", // Link
        "https://cdn.casterlabs.co/branding/casterlabs/icon.png", // imageLink
        -1, // Followers Count
        -1 // Subscriber Count
    );

    @SneakyThrows
    public static KoiEvent createTestEvent(@NonNull SimpleProfile streamer, @NonNull KoiEventType type) {
        KoiEvent result = null;

        switch (type) {
            case DONATION: // TODO Deprecate, in the future this'll fall through to RICH_MESSAGE.
                String currency = "USD";
                String image = "https://cdn.casterlabs.co/branding/casterlabs/icon.png";

                switch (streamer.getPlatform()) {
                    case CAFFEINE:
                        currency = "CAFFEINE_GOLD";
                        image = "https://assets.caffeine.tv/digital-items/Caffeine-TommytheClown-DynamicDuos-Props-Fireworks.4235957300.png";
                        break;

                    case TWITCH:
                        currency = "TWITCH_BITS";
                        image = "https://static-cdn.jtvnw.net/bits/dark/animated/purple/4";
                        break;

                    case TROVO:
                        currency = "TROVO_ELIXIR";
                        image = "https://static.trovo.live/imgupload/shop/20200509_9fikzpgc2ne.webp";
                        break;

                    case DLIVE:
                        currency = "DLIVE_LEMONS";
                        image = "https://dlive.tv/img/gift_lemon.1f2a2028.png";
                        break;

                    case TIKTOK:
                        currency = "TIKTOK_COINS";
                        image = "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/eba3a9bb85c33e017f3648eaf88d7189~tplv-obj.webp";
                        break;

                    case THETA:
                    case KICK:
                    case GLIMESH:
                    case YOUTUBE:
                    default:
                        break;
                }

                result = KoiEventType.get(
                    Rson.DEFAULT.fromJson(
                        "{\"upvotes\":0,\"visible\":true,\"is_highlighted\":false,\"mentions\":[],\"links\":[],\"sender\":{SENDER},\"message\":\"Have some candy!\",\"id\":\"-1\",\"meta_id\":\"-1\",\"emotes\":{},\"external_emotes\":{},\"donations\":[{\"type\":\"CASTERLABS_TEST\",\"name\":\"Test Donation\",\"currency\":\"{CURRENCY}\",\"amount\":0,\"image\":\"{IMAGE}\",\"animated_image\":\"{IMAGE}\"}]}"
                            .replace("{SENDER}", Rson.DEFAULT.toJson(testUser).toString())
                            .replace("{CURRENCY}", currency)
                            .replace("{IMAGE}", image),
                        JsonObject.class
                    )
                );
                break;

            case FOLLOW:
                result = new FollowEvent(testUser);
                break;

            case RAID:
                result = new RaidEvent(testUser, 0);
                break;

            case SUBSCRIPTION:
                result = new SubscriptionEvent(testUser, 0, null, SubscriptionType.SUB, SubscriptionLevel.UNKNOWN);
                break;

            case RICH_MESSAGE:
            case CLEARCHAT:
            case CHANNEL_POINTS:
            case CHAT:
            case CATCHUP:
            case META:
            case PLATFORM_MESSAGE:
            case STREAM_STATUS:
            case ROOMSTATE:
            case USER_UPDATE:
            case VIEWER_JOIN:
            case VIEWER_LEAVE:
            case VIEWER_LIST:
            default:
                break;
        }

        assert result != null : new IllegalArgumentException("Cannot create a test event for type: " + type);

        ReflectionLib.setValue(result, "streamer", streamer);
        ReflectionLib.setValue(result, "timestamp", Instant.now().toString());

        return result;
    }

}

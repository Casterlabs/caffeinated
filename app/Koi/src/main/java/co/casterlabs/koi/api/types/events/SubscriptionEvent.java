package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class SubscriptionEvent extends KoiEvent {
    private User subscriber;

    @JsonField("gift_recipient")
    private User giftRecipient;

    /**
     * @deprecated This value has been superseded by {@link #monthsPurchased} and
     *             {@link #monthsStreak}. This will always have a value of 1 until
     *             it's removal.
     */
    @Deprecated
    private int months = 1;

    /**
     * Note that this is unknowable on some platforms, like TikTok. In that case, it
     * will always be 1.
     */
    @JsonField("months_purchased")
    private int monthsPurchased;

    /**
     * Note that this is unknowable on some platforms, like TikTok. In that case, it
     * will always be 1.
     */
    @JsonField("months_streak")
    private int monthsStreak;

    @JsonField("sub_type")
    private SubscriptionType subType;

    @JsonField("sub_level")
    private SubscriptionLevel subLevel;

    @Override
    public KoiEventType getType() {
        return KoiEventType.SUBSCRIPTION;
    }

    public SubscriptionEvent(SimpleProfile streamer, User subscriber, User giftRecipient, int monthsPurchased, int monthsStreak, SubscriptionType subType, SubscriptionLevel subLevel) {
        this.streamer = streamer;
        this.subscriber = subscriber;
        this.giftRecipient = giftRecipient;
        this.monthsPurchased = monthsPurchased;
        this.monthsStreak = monthsStreak;
        this.subType = subType;
        this.subLevel = subLevel;
    }

    public static enum SubscriptionType {
        SUB,
        RESUB,

        SUBGIFT,
        RESUBGIFT,

        ANONSUBGIFT,
        ANONRESUBGIFT;

    }

    public static enum SubscriptionLevel {
        UNKNOWN,
        TWITCH_PRIME,
        TIER_1,
        TIER_2,
        TIER_3,
        TIER_4,
        TIER_5;

    }

}

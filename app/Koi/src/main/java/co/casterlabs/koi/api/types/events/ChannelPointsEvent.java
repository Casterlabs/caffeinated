package co.casterlabs.koi.api.types.events;

import java.time.Instant;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class ChannelPointsEvent extends KoiEvent {
    private User sender;
    private ChannelPointsReward reward;
    private RedemptionStatus status;
    private String id;
    private String message;

    @Override
    public KoiEventType getType() {
        return KoiEventType.CHANNEL_POINTS;
    }

    public static enum RedemptionStatus {
        FULFILLED,
        UNFULFILLED
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class ChannelPointsReward {

        @JsonField("background_color")
        private String backgroundColor;

        private String id;

        @JsonField("cooldown_expires_at")
        private String cooldownExpiresAt;

        public Instant getCooldownExpiresAt() {
            return Instant.parse(this.cooldownExpiresAt);
        }

        private String title;

        private String prompt;

        private int cost;

        @JsonField("is_enabled")
        private boolean enabled;

        @JsonField("is_in_stock")
        private boolean inStock;

        @JsonField("is_paused")
        private boolean paused;

        @JsonField("is_sub_only")
        private boolean subOnly;

        @JsonField("is_user_input_required")
        private boolean userInputRequired;

        @JsonField("reward_image")
        private String rewardImage;

        @JsonField("default_reward_image")
        private String defaultRewardImage;

        @JsonField("max_per_stream")
        private ChannelPointsMaxPerStream maxPerStream;

        @JsonField("max_per_user_per_stream")
        private ChannelPointsMaxPerUserPerStream maxPerUserPerStream;

        @JsonField("global_cooldown")
        private ChannelPointsCooldown globalCooldown;

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class ChannelPointsMaxPerStream {

        @JsonField("is_enabled")
        private boolean enabled;

        @JsonField("max_per_stream")
        private int max;

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class ChannelPointsMaxPerUserPerStream {

        @JsonField("is_enabled")
        private boolean enabled;

        @JsonField("max_per_user_per_stream")
        private int max;

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class ChannelPointsCooldown {

        @JsonField("is_enabled")
        private boolean enabled;

        @JsonField("global_cooldown_seconds")
        private int globalCooldownSeconds;

    }

}

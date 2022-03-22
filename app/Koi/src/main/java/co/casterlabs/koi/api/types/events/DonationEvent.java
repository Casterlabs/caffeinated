package co.casterlabs.koi.api.types.events;

import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class DonationEvent extends ChatEvent {
    private List<Donation> donations;

    @Override
    public KoiEventType getType() {
        return KoiEventType.DONATION;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Donation {
        @JsonField("animated_image")
        private String animatedImage;
        private String currency;
        private double amount;
        private String image;
        private DonationType type;
        private String name;

    }

    public static enum DonationType {
        TWITCH_BITS,
        CAFFEINE_PROP,
        CASTERLABS_TEST,
        TROVO_SPELL;

    }

}

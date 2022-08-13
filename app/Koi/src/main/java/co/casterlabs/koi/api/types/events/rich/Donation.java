package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class Donation {
    @JsonField("animated_image")
    private String animatedImage;
    private String currency;
    private double amount;
    private String image;
    private Donation.DonationType type;
    private String name;

    public static enum DonationType {
        TWITCH_BITS,
        CAFFEINE_PROP,
        CASTERLABS_TEST,
        TROVO_SPELL,
        SUPER_CHAT,
        SUPER_STICKER;

    }
}

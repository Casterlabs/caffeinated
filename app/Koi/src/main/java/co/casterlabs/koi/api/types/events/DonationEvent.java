package co.casterlabs.koi.api.types.events;

import java.util.List;

import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Deprecated use {@link RichMessageEvent}
 */
@Deprecated
@Getter
@ToString
@NoArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class DonationEvent extends ChatEvent {
    private List<Donation> donations;

    public DonationEvent(SimpleProfile streamer, User sender, String id, String message, List<Donation> donations) {
        super(streamer, sender, id, message);
        this.donations = donations;
    }

    @Override
    public KoiEventType getType() {
        return KoiEventType.DONATION;
    }

}

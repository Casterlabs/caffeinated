package co.casterlabs.koi.api.types.events;

import java.util.List;

import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @Deprecated use {@link RichMessageEvent}
 */
@Deprecated
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

}

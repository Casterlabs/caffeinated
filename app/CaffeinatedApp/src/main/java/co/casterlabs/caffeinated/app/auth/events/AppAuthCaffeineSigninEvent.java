package co.casterlabs.caffeinated.app.auth.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppAuthCaffeineSigninEvent extends AbstractCancellableEvent<AppAuthEventType> {
    private String token;

    public AppAuthCaffeineSigninEvent() {
        super(AppAuthEventType.CAFFEINE_SIGNIN);
    }

    @JsonValidate
    private void validate() {
        assert this.token != null;
    }

}

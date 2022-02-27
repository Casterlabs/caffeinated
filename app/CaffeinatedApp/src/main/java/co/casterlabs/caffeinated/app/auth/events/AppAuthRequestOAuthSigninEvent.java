package co.casterlabs.caffeinated.app.auth.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppAuthRequestOAuthSigninEvent extends AbstractCancellableEvent<AppAuthEventType> {
    private String platform;
    private boolean isKoi;
    private boolean goBack = true;

    public AppAuthRequestOAuthSigninEvent() {
        super(AppAuthEventType.REQUEST_OAUTH_SIGNIN);
    }

    @JsonValidate
    private void validate() {
        assert this.platform != null;
    }

}

package co.casterlabs.caffeinated.app.music_integration.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppMusicIntegrationSignoutEvent extends AbstractCancellableEvent<AppMusicIntegrationEventType> {
    private String platform;

    public AppMusicIntegrationSignoutEvent() {
        super(AppMusicIntegrationEventType.SIGNOUT);
    }

    @JsonValidate
    private void validate() {
        assert this.platform != null;
    }

}

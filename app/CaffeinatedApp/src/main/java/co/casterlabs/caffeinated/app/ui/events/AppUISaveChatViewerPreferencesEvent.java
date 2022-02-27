package co.casterlabs.caffeinated.app.ui.events;

import co.casterlabs.caffeinated.app.ui.UIPreferences.ChatViewerPreferences;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppUISaveChatViewerPreferencesEvent extends AbstractCancellableEvent<AppUIEventType> {
    private ChatViewerPreferences preferences;

    public AppUISaveChatViewerPreferencesEvent() {
        super(AppUIEventType.SAVE_CHAT_VIEWER_PREFERENCES);
    }

    @JsonValidate
    private void validate() {
        assert this.preferences != null;
    }

}

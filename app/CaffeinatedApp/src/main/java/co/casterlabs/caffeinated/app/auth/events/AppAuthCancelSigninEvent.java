package co.casterlabs.caffeinated.app.auth.events;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppAuthCancelSigninEvent extends AbstractCancellableEvent<AppAuthEventType> {

    public AppAuthCancelSigninEvent() {
        super(AppAuthEventType.CANCEL_SIGNIN);
    }

}

package co.casterlabs.caffeinated.app.ui.events;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppUIThemeLoadedEvent extends AbstractCancellableEvent<AppUIEventType> {

    public AppUIThemeLoadedEvent() {
        super(AppUIEventType.THEME_LOADED);
    }

}

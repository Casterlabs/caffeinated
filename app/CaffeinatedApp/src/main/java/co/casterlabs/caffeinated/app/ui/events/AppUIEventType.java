package co.casterlabs.caffeinated.app.ui.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppUIEventType {
    APPEARANCE_UPDATE(AppUIAppearanceUpdateEvent.class),
    THEME_LOADED(AppUIThemeLoadedEvent.class),
    OPENLINK(AppUIOpenLinkEvent.class),
    SAVE_CHAT_VIEWER_PREFERENCES(AppUISaveChatViewerPreferencesEvent.class);

    private @Getter Class<AbstractEvent<AppUIEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppUIEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppUIEventType>>) clazz;
    }

}

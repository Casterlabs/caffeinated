package co.casterlabs.caffeinated.app.music_integration.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppMusicIntegrationEventType {
    SIGNOUT(AppMusicIntegrationSignoutEvent.class),
    SETTINGS_UPDATE(AppMusicIntegrationSettingsUpdateEvent.class);

    private @Getter Class<AbstractEvent<AppMusicIntegrationEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppMusicIntegrationEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppMusicIntegrationEventType>>) clazz;
    }

}

package co.casterlabs.caffeinated.app.plugins.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppPluginIntegrationEventType {
    CREATE_WIDGET(AppPluginIntegrationCreateWidgetEvent.class),
    RENAME_WIDGET(AppPluginIntegrationRenameWidgetEvent.class),
    DELETE_WIDGET(AppPluginIntegrationDeleteWidgetEvent.class),
    EDIT_WIDGET_SETTINGS(AppPluginIntegrationEditWidgetSettingsEvent.class),
    COPY_WIDGET_URL(AppPluginIntegrationCopyWidgetUrlEvent.class),
    CLICK_WIDGET_SETTINGS_BUTTON(AppPluginIntegrationClickWidgetSettingsButtonEvent.class);

    private @Getter Class<AbstractEvent<AppPluginIntegrationEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppPluginIntegrationEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppPluginIntegrationEventType>>) clazz;
    }

}

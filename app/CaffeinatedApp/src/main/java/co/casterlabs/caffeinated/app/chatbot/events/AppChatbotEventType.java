package co.casterlabs.caffeinated.app.chatbot.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppChatbotEventType {

    ;

    private @Getter Class<AbstractEvent<AppChatbotEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppChatbotEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppChatbotEventType>>) clazz;
    }

}

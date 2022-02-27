package co.casterlabs.caffeinated.app.auth.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppAuthEventType {
    REQUEST_OAUTH_SIGNIN(AppAuthRequestOAuthSigninEvent.class),
    CAFFEINE_SIGNIN(AppAuthCaffeineSigninEvent.class),
    SIGNOUT(AppAuthSignoutEvent.class),
    CANCEL_SIGNIN(AppAuthCancelSigninEvent.class);

    private @Getter Class<AbstractEvent<AppAuthEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppAuthEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppAuthEventType>>) clazz;
    }

}

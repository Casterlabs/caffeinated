package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.util.EventBus;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.rakurai.json.element.JsonObject;

public enum AppEventBus {
    /**
     * {@link JsonObject}
     */
    AUTH_PLATFORMS,
    /**
     * {@link JsonObject}
     */
    AUTH_COMPLETION,

    /**
     * {@link JsonObject}
     */
    KOI_STATICS,
    /**
     * {@link KoiEvent}
     */
    KOI_EVENT,

    /**
     * {@link JsonObject}
     */
    MUSIC_UPDATE,

    /**
     * {@link JsonObject}
     */
    APPEARANCE_UPDATE,
    ;

    public static final EventBus<AppEventBus, Object> bus = new EventBus<>();

}

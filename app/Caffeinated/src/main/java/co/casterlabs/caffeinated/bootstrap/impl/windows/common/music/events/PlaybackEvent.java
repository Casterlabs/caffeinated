package co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class PlaybackEvent<T> {
    private PlaybackEventType type;
    private String sessionId;
    private T data;

    public static enum PlaybackEventType {
        PROPERTY_CHANGE,
        PLAYBACK_STATE_CHANGE,
        SESSION_CLOSED,
        SESSION_STARTED

    }

}

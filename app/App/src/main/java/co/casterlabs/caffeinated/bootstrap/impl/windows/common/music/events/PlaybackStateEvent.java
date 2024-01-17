package co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.events;

import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.PlaybackStatus;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.PlaybackType;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PlaybackStateEvent {
    private PlaybackType type;
    private PlaybackStatus status;

    @JsonDeserializationMethod("PlaybackType")
    private void $deserialize_PlaybackType(JsonElement e) {
        this.type = PlaybackType.valueOf(e.getAsString().toUpperCase());
    }

    @JsonDeserializationMethod("PlaybackStatus")
    private void $deserialize_PlaybackStatus(JsonElement e) {
        this.status = PlaybackStatus.valueOf(e.getAsString().toUpperCase());
    }

}

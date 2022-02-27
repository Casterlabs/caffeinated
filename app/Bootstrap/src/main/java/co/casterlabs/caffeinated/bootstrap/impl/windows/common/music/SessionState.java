package co.casterlabs.caffeinated.bootstrap.impl.windows.common.music;

import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.MediaInfo;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.PlaybackStatus;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types.PlaybackType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@Accessors(chain = true)
@RequiredArgsConstructor
public class SessionState {
    private final String sessionId;

    private PlaybackType type;
    private PlaybackStatus status;
    private @ToString.Exclude MediaInfo mediaInfo;

}

package co.casterlabs.caffeinated.pluginsdk.music;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class MusicTrack {
    private @NonNull String title;
    private @NonNull List<String> artists;
    private @Nullable String album;
    private @NonNull String albumArtUrl;
    private @NonNull String link;

}

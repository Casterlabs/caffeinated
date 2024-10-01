package co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.types;

import java.util.List;

import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class MediaInfo {

    @JsonField("AlbumArtist")
    private String albumArtist;

    @JsonField("AlbumTitle")
    private String albumTitle;

    @JsonField("AlbumTrackCount")
    private int albumTrackCount;

    @JsonField("Artist")
    private String artist;

    @JsonField("Genres")
    private List<String> genres;

    private PlaybackType playbackType;

    @JsonField("Subtitle")
    private String subtitle;

    @JsonField("Thumbnail")
    private String thumbnailBase64;

    @JsonField("ThumbnailType")
    private String thumbnailMimeType;

    @JsonField("Title")
    private String title;

    @JsonField("TrackNumber")
    private int trackNumber;

    @JsonDeserializationMethod("PlaybackType")
    private void $deserialize_PlaybackType(JsonString str) {
        this.playbackType = PlaybackType.valueOf(str.getAsString().toUpperCase());
    }

    public @NonNull String getThumnailAsDataUri() {
        if (this.thumbnailBase64 == null) {
            return MusicProvider.BLANK_ART;
        } else {
            return "data:" + this.thumbnailMimeType + ";base64," + this.thumbnailBase64;
        }
    }

}

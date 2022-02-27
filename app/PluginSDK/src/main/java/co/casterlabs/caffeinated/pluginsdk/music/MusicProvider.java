package co.casterlabs.caffeinated.pluginsdk.music;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

@JsonClass(serializer = Serializer.class)
public interface MusicProvider {
    public static final String BLANK_ART = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNM/g8AAcsBZMiygSIAAAAASUVORK5CYII=";

    public String getServiceName();

    public String getServiceId();

    public String getAccountName();

    public String getAccountLink();

    public MusicPlaybackState getPlaybackState();

    public MusicTrack getCurrentTrack();

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    default JsonObject toJson() {
        return new JsonObject()
            .put("serviceName", this.getServiceName())
            .put("serviceId", this.getServiceId())
            .put("accountName", this.getAccountName())
            .put("accountLink", this.getAccountLink())
            .put("playbackState", this.getPlaybackState().name())
            .put("currentTrack", Rson.DEFAULT.toJson(this.getCurrentTrack()));
    }

}

class Serializer implements JsonSerializer<MusicProvider> {

    public JsonElement serialize(@NonNull MusicProvider value, @NonNull Rson rson) {
        return value.toJson();
    }

}

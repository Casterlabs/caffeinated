package co.casterlabs.caffeinated.app.music_integration;

import co.casterlabs.caffeinated.app.music_integration.impl.InternalMusicProvider;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.yen.Cacheable;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class MusicProviderSettings implements Cacheable {
    private String serviceId;
    private JsonElement json;

    @Override
    public String id() {
        return this.serviceId;
    }

    public static MusicProviderSettings from(InternalMusicProvider<?> provider) {
        return from(
            provider.getServiceId(),
            Rson.DEFAULT.toJson(provider.getSettings())
        );
    }

    public static MusicProviderSettings from(String serviceId, JsonElement json) {
        MusicProviderSettings mps = new MusicProviderSettings();
        mps.serviceId = serviceId;
        mps.json = json;
        return mps;
    }

}
